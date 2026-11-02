import json
import os
import re
import urllib.parse
import urllib.request
from datetime import datetime, timezone

import boto3

RED = 0xE74C3C
GREEN = 0x2ECC71
ORANGE = 0xE67E22
GRAY = 0x95A5A6
TITLE_LIMIT = 256
DESCRIPTION_LIMIT = 4096
BUDGETS_CONSOLE_URL = "https://console.aws.amazon.com/costmanagement/home#/budgets"
WHAT, WHY, HOW = "What's happening?", "Possible causes", "How to troubleshoot?"

_webhook_url = None


def webhook_url():
    global _webhook_url
    if _webhook_url is None:
        secret = boto3.client("secretsmanager").get_secret_value(SecretId=os.environ["WEBHOOK_SECRET_ARN"])
        _webhook_url = secret["SecretString"].strip()
    return _webhook_url


# Datadog multi-alert처럼 상태와 환경을 제목 앞에 둔다
def title_with_status(status, title):
    environment = os.environ.get("ENVIRONMENT")
    prefix = f"[{status} on {environment}]" if environment else f"[{status}]"
    return f"{prefix} {title}"


def as_text(value):
    return "\n".join(f"- {item}" for item in value) if isinstance(value, list) else value


# 소제목은 한 줄에 두고 내용은 다음 줄에, 칸 사이는 빈 줄로 띄운다. 여러 항목은 불릿으로 쓴다
def sections(what, why=None, how=None):
    return "\n\n".join(f"**{name}**\n{as_text(value)}" for name, value in [(WHAT, what), (WHY, why), (HOW, how)] if value)


def iso_timestamp(value):
    for fmt in ("%Y-%m-%dT%H:%M:%S.%f%z", "%Y-%m-%dT%H:%M:%S.%fZ"):
        try:
            return datetime.strptime(value, fmt).isoformat()
        except (TypeError, ValueError):
            continue
    return None


def alarm_console_url(alarm_name):
    region = os.environ.get("AWS_REGION", "ap-northeast-2")
    return f"https://{region}.console.aws.amazon.com/cloudwatch/home?region={region}#alarmsV2:alarm/{urllib.parse.quote(alarm_name)}"


COMPARISONS = {
    "GreaterThanOrEqualToThreshold": "≥",
    "GreaterThanThreshold": ">",
    "LessThanThreshold": "<",
    "LessThanOrEqualToThreshold": "≤",
}


# 측정값은 소수 한 자리까지, 기준값은 불필요한 0 없이 쓴다
def format_value(value, unit, precise=True):
    if value is None:
        return "알 수 없음"
    if unit == "bytes":
        value /= 1024 ** 3
    number = f"{value:.1f}" if precise and unit in ("percent", "bytes") else f"{value:g}"
    return number + {"percent": "%", "bytes": "GB", "count": "건"}.get(unit, "")


# NewStateReason 예: "... datapoints [93.2 (02/11/26 08:00:00)] was greater than the threshold (90.0) ..."
def alarm_value(alarm):
    match = re.search(r"\[([-\d.]+) \(", alarm.get("NewStateReason", ""))
    return float(match[1]) if match else None


def alarm_started_at(alarm_name):
    history = boto3.client("cloudwatch").describe_alarm_history(
        AlarmName=alarm_name, HistoryItemType="StateUpdate", ScanBy="TimestampDescending", MaxRecords=10)
    for item in history["AlarmHistoryItems"]:
        if json.loads(item["HistoryData"])["newState"]["stateValue"] == "ALARM":
            return item["Timestamp"]
    return None


def format_duration(started_at):
    minutes = int((datetime.now(timezone.utc) - started_at).total_seconds() // 60)
    hours, minutes = divmod(minutes, 60)
    return f"{hours}시간 {minutes}분" if hours else f"{max(minutes, 1)}분"


# alarm_description은 JSON(title, what, why, how, unit)이다. what의 {value}는 측정값, {condition}은 '> 90%' 같은 조건으로 채운다
# alarm을 처음 만들 때도 OK로 바뀌므로 ALARM에서 돌아온 경우만 복구로 보낸다
def alarm_embed(alarm):
    recovered = alarm.get("NewStateValue") == "OK"
    if recovered and alarm.get("OldStateValue") != "ALARM":
        return None

    try:
        spec = json.loads(alarm.get("AlarmDescription") or "")
    except json.JSONDecodeError:
        spec = {"title": alarm.get("AlarmDescription") or alarm["AlarmName"]}

    unit = spec.get("unit")
    trigger = alarm.get("Trigger", {})
    value = format_value(alarm_value(alarm), unit)
    condition = f"{COMPARISONS.get(trigger.get('ComparisonOperator'), '')} {format_value(trigger.get('Threshold'), unit, precise=False)}".strip()

    if recovered:
        started_at = alarm_started_at(alarm["AlarmName"])
        duration = f"{format_duration(started_at)} 지속 후 복구" if started_at else "복구"
        body = sections(f"현재 {value} ({duration})")
    else:
        what = spec.get("what", alarm.get("NewStateReason", "")).format(value=value, condition=condition)
        body = sections(what, spec.get("why"), spec.get("how"))

    return {
        "title": title_with_status("Recovered" if recovered else "Triggered", spec["title"]),
        "description": body,
        "url": alarm_console_url(alarm["AlarmName"]),
        "color": GREEN if recovered else RED,
        "footer": {"text": alarm["AlarmName"]},
        "timestamp": iso_timestamp(alarm.get("StateChangeTime")),
    }, not recovered


ECS_EVENTS = {
    "SERVICE_TASK_START_IMPAIRED": (
        "Task failing to start",
        ["앱 시작 오류", "secret 누락", "DB 연결 실패"],
        ["앱 로그 확인", "secret 값 확인"],
    ),
    "SERVICE_TASK_PLACEMENT_FAILURE": (
        "Task placement failed",
        "Fargate Spot 용량 부족",
        ["ECS 자동 재시도 중", "해결되지 않으면 일반 Fargate 전환 검토"],
    ),
    "SERVICE_DEPLOYMENT_FAILED": (
        "Deployment failed and rolled back",
        ["새 버전 시작 실패", "헬스 체크 실패"],
        ["Deploy 워크플로 확인", "앱 로그 확인"],
    ),
}


def ecs_service_console_url(service_arn):
    region = os.environ.get("AWS_REGION", "ap-northeast-2")
    cluster, service = service_arn.split(":service/")[1].split("/")
    return f"https://{region}.console.aws.amazon.com/ecs/v2/clusters/{cluster}/services/{service}/events?region={region}"


def ecs_embed(event):
    detail = event["detail"]
    title, why, how = ECS_EVENTS.get(detail["eventName"], (detail["eventName"], None, None))
    service_arn = event["resources"][0]
    service = service_arn.rsplit("/", 1)[-1]
    return {
        "title": title_with_status("Triggered", f"{title} ({service})"),
        "description": sections(detail.get("reason") or "상세 사유 미제공 (ECS 서비스 이벤트 확인 필요)", why, how),
        "url": ecs_service_console_url(service_arn),
        "color": RED,
        "footer": {"text": service},
        "timestamp": event.get("time"),
    }, True


# CD 워크플로가 배포를 마친 뒤 발행하는 메시지
def deploy_embed(message):
    commit = message["commit"]
    author = message.get("author")
    return {
        "title": title_with_status("Deployed", message.get("message", "").partition("\n")[0] or commit[:7]),
        "description": f"**Commit**\n{commit[:7]}" + (f" ({author})" if author else ""),
        "url": message.get("url"),
        "color": GREEN,
        "footer": {"text": commit[:7]},
        "timestamp": message.get("time"),
    }, False


# Budgets 메시지 형식이 바뀌어 값을 못 찾으면 None을 돌려 원문을 보내게 한다
def budget_embed(message, timestamp):
    found = dict(re.findall(r"^(Budget Name|Budgeted Amount|Alert Threshold|ACTUAL Amount|FORECASTED Amount): (.+)$", message, re.MULTILINE))
    name = found.get("Budget Name")
    budgeted = found.get("Budgeted Amount")
    threshold = found.get("Alert Threshold")
    if "FORECASTED Amount" in found:
        title, amount_label, amount = "Budget forecast exceeded", "예상 비용", found["FORECASTED Amount"]
    else:
        title, amount_label, amount = "Budget exceeded", "현재 비용", found.get("ACTUAL Amount")

    if not all([name, budgeted, threshold, amount]):
        return None
    # Alert Threshold 예: "> $24.00 (80.00%)". 형식이 다르면 원문을 그대로 쓴다
    limit = re.search(r"[<>]=?\s*\$[\d,.]+", threshold)
    percent = re.search(r"([\d.]+)%", threshold)
    condition = ", ".join(filter(None, [limit and limit[0], percent and f"예산의 {float(percent[1]):g}%"])) or threshold
    if percent:
        title = f"{title} ({float(percent[1]):g}%)"
    return {
        "title": title_with_status("Triggered", title),
        "description": sections(
            [f"{amount_label} {amount} ({condition})", f"월 예산 {budgeted}"],
            ["새 리소스 추가", "야간 중지 꺼짐 등 사용량 증가"],
            "Cost Explorer에서 서비스별 비용 확인",
        ),
        "url": BUDGETS_CONSOLE_URL,
        "color": ORANGE,
        "footer": {"text": name},
        "timestamp": timestamp,
    }


# (embed, 멘션 여부)를 돌려준다. 문제가 생겼을 때만 멘션하고, 보낼 필요가 없으면 None
def to_embed(sns):
    timestamp = iso_timestamp(sns.get("Timestamp"))
    try:
        message = json.loads(sns["Message"])
    except json.JSONDecodeError:
        message = None

    if isinstance(message, dict) and "AlarmName" in message:
        return alarm_embed(message)
    if isinstance(message, dict) and message.get("source") == "aws.ecs":
        return ecs_embed(message)
    if isinstance(message, dict) and message.get("source") == "deepple.deploy":
        return deploy_embed(message)

    subject = sns.get("Subject") or "AWS notification"
    if subject.startswith("AWS Budgets"):
        embed = budget_embed(sns["Message"], timestamp)
        if embed:
            return embed, True

    return {"title": title_with_status("Triggered", subject), "description": sns["Message"], "color": GRAY, "timestamp": timestamp}, True


def post(embed, mention):
    embed["title"] = embed["title"][:TITLE_LIMIT]
    embed["description"] = embed.get("description", "")[:DESCRIPTION_LIMIT]
    embed = {key: value for key, value in embed.items() if value}

    # 푸시 알림에는 embed가 아니라 content만 보이므로 제목을 앞에 둔다
    role_id = os.environ.get("MENTION_ROLE_ID") if mention else None
    payload = {
        "content": f"{embed['title']} <@&{role_id}>" if role_id else embed["title"],
        "embeds": [embed],
        "allowed_mentions": {"roles": [role_id] if role_id else []},
    }
    request = urllib.request.Request(
        webhook_url(),
        data=json.dumps(payload).encode(),
        headers={
            "Content-Type": "application/json",
            # 기본 Python User-Agent는 Discord(Cloudflare)가 차단한다
            "User-Agent": "deepple-alerts/1.0",
        },
    )
    with urllib.request.urlopen(request, timeout=10):
        pass


def handler(event, context):
    for record in event["Records"]:
        result = to_embed(record["Sns"])
        if result:
            post(*result)
