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


def title_with_status(status, title):
    return f"[{status}] {title}"


def bullets(value):
    return "\n".join(f"- {item}" for item in (value if isinstance(value, list) else [value]))


# 소제목은 한 줄에 두고 내용은 다음 줄에, 칸 사이는 빈 줄로 띄운다. 원인과 확인 방법은 불릿으로 쓴다
def sections(what, why=None, how=None):
    parts = [f"**{WHAT}**\n{what}"] if what else []
    parts += [f"**{name}**\n{bullets(value)}" for name, value in [(WHY, why), (HOW, how)] if value]
    return "\n\n".join(parts)


def parse_time(value):
    for fmt in ("%Y-%m-%dT%H:%M:%S.%f%z", "%Y-%m-%dT%H:%M:%S.%fZ"):
        try:
            parsed = datetime.strptime(value, fmt)
            return parsed if parsed.tzinfo else parsed.replace(tzinfo=timezone.utc)
        except (TypeError, ValueError):
            continue
    return None


def format_duration(started_at, ended_at):
    return f"{max(int((ended_at - started_at).total_seconds() // 60), 1)}분 지속"


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

    if recovered:
        started_at = alarm_started_at(alarm["AlarmName"])
        ended_at = parse_time(alarm.get("StateChangeTime")) or datetime.now(timezone.utc)
        body = format_duration(started_at, ended_at) if started_at else "복구"
    else:
        unit = spec.get("unit")
        trigger = alarm.get("Trigger", {})
        value = format_value(alarm_value(alarm), unit)
        condition = f"{COMPARISONS.get(trigger.get('ComparisonOperator'), '')} {format_value(trigger.get('Threshold'), unit, precise=False)}".strip()
        what = spec.get("what", alarm.get("NewStateReason", "")).format(value=value, condition=condition)
        body = sections(what, spec.get("why"), spec.get("how"))

    return {
        "title": title_with_status("Recovered" if recovered else "Triggered", spec["title"]),
        "description": body,
        "url": alarm_console_url(alarm["AlarmName"]),
        "color": GREEN if recovered else RED,
    }, not recovered


# (제목, 원인, 확인 방법, 멘션 여부). 확인 방법의 {log_group}은 서비스의 로그 그룹으로 채운다
ECS_EVENTS = {
    "SERVICE_TASK_START_IMPAIRED": (
        "Task failing to start",
        ["앱 시작 오류 (설정 누락, DB·Redis 연결 실패)", "secret 값 누락", "이미지 pull 실패"],
        ["CloudWatch `{log_group}`에서 해당 서비스 로그 확인", "ECS 서비스 이벤트에서 중지 사유 확인"],
        True,
    ),
    "SERVICE_TASK_PLACEMENT_FAILURE": (
        "Task placement failed",
        ["Fargate Spot 용량 부족"],
        ["ECS가 자동으로 재시도하므로 몇 분 기다린 뒤 서비스 상태 확인", "계속 실패하면 capacity provider를 FARGATE로 바꿔 apply"],
        False,
    ),
    "SERVICE_DEPLOYMENT_FAILED": (
        "Deployment failed and rolled back",
        ["새 버전 앱 시작 실패 (설정 누락, Flyway 마이그레이션 실패)", "ALB 헬스 체크 실패"],
        ["GitHub Actions Deploy 실행 로그 확인", "CloudWatch `{log_group}`에서 새 task 로그 확인"],
        True,
    ),
}


def ecs_service_console_url(cluster, service):
    region = os.environ.get("AWS_REGION", "ap-northeast-2")
    return f"https://{region}.console.aws.amazon.com/ecs/v2/clusters/{cluster}/services/{service}/events?region={region}"


def ecs_embed(event):
    detail = event["detail"]
    title, why, how, mention = ECS_EVENTS.get(detail["eventName"], (detail["eventName"], None, None, True))
    cluster, service = event["resources"][0].split(":service/")[1].split("/")
    return {
        "title": title_with_status("Triggered", f"{title} ({service})"),
        "description": sections(
            detail.get("reason") or "상세 사유 미제공 (ECS 서비스 이벤트 확인 필요)",
            why,
            [step.format(log_group=f"/ecs/{cluster}") for step in how] if how else None,
        ),
        "url": ecs_service_console_url(cluster, service),
        "color": RED,
    }, mention


# CD 워크플로가 배포를 마친 뒤 발행하는 메시지
def deploy_embed(message):
    commit = message["commit"][:7]
    author = message.get("author")
    return {
        "title": title_with_status("Deployed", message.get("message", "").partition("\n")[0] or commit),
        "description": f"{commit} · {author}" if author else commit,
        "url": message.get("url"),
        "color": GREEN,
    }, False


# Budgets 메시지 형식이 바뀌어 값을 못 찾으면 None을 돌려 원문을 보내게 한다
def budget_embed(message):
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
    # Alert Threshold 예: "> $24.00 (80.00%)". 비율이 없으면 원문을 그대로 쓴다
    percent = re.search(r"([\d.]+)%", threshold)
    exceeded = f"{float(percent[1]):g}% 초과" if percent else threshold
    if percent:
        title = f"{title} {float(percent[1]):g}%"
    return {
        "title": title_with_status("Triggered", f"{title} ({name})"),
        "description": sections(
            f"{amount_label} {amount} / 월 예산 {budgeted} ({exceeded})",
            ["새 리소스 추가", "야간 중지 꺼짐 등 사용량 증가"],
            ["Cost Explorer에서 서비스별 비용 확인"],
        ),
        "url": BUDGETS_CONSOLE_URL,
        "color": ORANGE,
    }


# Cost Anomaly Detection의 IMMEDIATE 구독 메시지
def anomaly_embed(message):
    impact = message.get("impact", {})
    causes = message.get("rootCauses") or []
    service = (causes[0].get("service") if causes else None) or message.get("dimensionalValue")
    what = f"예상 ${impact.get('totalExpectedSpend', 0):.2f} → 실제 ${impact.get('totalActualSpend', 0):.2f} (+${impact.get('totalImpact', 0):.2f})"
    why = [" · ".join(filter(None, [cause.get("service"), cause.get("region"), cause.get("usageType")])) for cause in causes[:3]]
    return {
        "title": title_with_status("Triggered", f"Cost anomaly ({service})" if service else "Cost anomaly"),
        "description": sections(what, [item for item in why if item] or None, ["Cost Anomaly Detection 상세 화면에서 원인 확인", "예상하지 못한 리소스면 정리, 키 유출이 의심되면 즉시 키 비활성화"]),
        "url": message.get("anomalyDetailsLink"),
        "color": ORANGE,
    }, True


# (embed, 멘션 여부)를 돌려준다. 문제가 생겼을 때만 멘션하고, 보낼 필요가 없으면 None
def to_embed(sns):
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
    if isinstance(message, dict) and "anomalyId" in message:
        return anomaly_embed(message)

    subject = sns.get("Subject") or "AWS notification"
    if subject.startswith("AWS Budgets"):
        embed = budget_embed(sns["Message"])
        if embed:
            return embed, True

    return {"title": title_with_status("Triggered", subject), "description": sns["Message"], "color": GRAY}, True


def post(embed, mention):
    embed["title"] = embed["title"][:TITLE_LIMIT]
    embed["description"] = embed.get("description", "")[:DESCRIPTION_LIMIT]
    embed = {key: value for key, value in embed.items() if value}

    # 채널은 멘션 알림만 받고 푸시에는 content만 보이므로, 멘션할 때만 content에 제목을 둔다
    role_id = os.environ.get("MENTION_ROLE_ID") if mention else None
    payload = {"embeds": [embed], "allowed_mentions": {"roles": [role_id] if role_id else []}}
    if role_id:
        payload["content"] = f"{embed['title']} <@&{role_id}>"
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
