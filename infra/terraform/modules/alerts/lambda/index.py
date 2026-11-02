import json
import os
import re
import urllib.parse
import urllib.request
from datetime import datetime

import boto3

RED = 0xE74C3C
GREEN = 0x2ECC71
ORANGE = 0xE67E22
GRAY = 0x95A5A6
TITLE_LIMIT = 256
DESCRIPTION_LIMIT = 4096
BUDGETS_CONSOLE_URL = "https://console.aws.amazon.com/costmanagement/home#/budgets"

_webhook_url = None


def webhook_url():
    global _webhook_url
    if _webhook_url is None:
        secret = boto3.client("secretsmanager").get_secret_value(SecretId=os.environ["WEBHOOK_SECRET_ARN"])
        _webhook_url = secret["SecretString"].strip()
    return _webhook_url


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


# alarm 설명의 첫 줄은 제목, 나머지는 본문으로 쓴다
# alarm을 처음 만들 때도 OK로 바뀌므로 ALARM에서 돌아온 경우만 복구로 보낸다
def alarm_embed(alarm):
    title, _, body = (alarm.get("AlarmDescription") or alarm["AlarmName"]).partition("\n")
    recovered = alarm.get("NewStateValue") == "OK"
    if recovered and alarm.get("OldStateValue") != "ALARM":
        return None
    return {
        "title": title,
        "description": "정상으로 돌아왔습니다." if recovered else body,
        "url": alarm_console_url(alarm["AlarmName"]),
        "color": GREEN if recovered else RED,
        "footer": {"text": alarm["AlarmName"]},
        "timestamp": iso_timestamp(alarm.get("StateChangeTime")),
    }, not recovered


ECS_EVENT_TITLES = {
    "SERVICE_TASK_START_IMPAIRED": ("task 시작 실패", "task가 계속 시작하지 못하고 있습니다. 로그와 secret 값을 확인하세요."),
    "SERVICE_DEPLOYMENT_FAILED": ("배포 실패", "배포가 실패해 이전 버전으로 되돌렸습니다."),
}


def ecs_service_console_url(service_arn):
    region = os.environ.get("AWS_REGION", "ap-northeast-2")
    cluster, service = service_arn.split(":service/")[1].split("/")
    return f"https://{region}.console.aws.amazon.com/ecs/v2/clusters/{cluster}/services/{service}/events?region={region}"


def ecs_embed(event):
    detail = event["detail"]
    title, description = ECS_EVENT_TITLES.get(detail["eventName"], (detail["eventName"], ""))
    service_arn = event["resources"][0]
    return {
        "title": title,
        "description": "\n".join(filter(None, [description, detail.get("reason")])),
        "url": ecs_service_console_url(service_arn),
        "color": RED,
        "footer": {"text": service_arn.rsplit("/", 1)[-1]},
        "timestamp": event.get("time"),
    }, True


# Budgets 메시지 형식이 바뀌어 값을 못 찾으면 None을 돌려 원문을 보내게 한다
def budget_embed(message, timestamp):
    fields = dict(re.findall(r"^(Budget Name|Budgeted Amount|Alert Threshold|ACTUAL Amount|FORECASTED Amount): (.+)$", message, re.MULTILINE))
    name = fields.get("Budget Name")
    budgeted = fields.get("Budgeted Amount")
    threshold = fields.get("Alert Threshold")
    if "FORECASTED Amount" in fields:
        amount_label, amount = "예상 비용", fields["FORECASTED Amount"]
        description = "이번 달 비용이 기준을 넘을 것으로 예상됩니다."
    else:
        amount_label, amount = "현재 비용", fields.get("ACTUAL Amount")
        description = "이번 달 비용이 기준을 넘었습니다."

    if not all([name, budgeted, threshold, amount]):
        return None
    return {
        "title": "예산 알림",
        "description": description,
        "url": BUDGETS_CONSOLE_URL,
        "color": ORANGE,
        "fields": [
            {"name": amount_label, "value": amount, "inline": True},
            {"name": "기준", "value": threshold, "inline": True},
            {"name": "예산", "value": budgeted, "inline": True},
        ],
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

    subject = sns.get("Subject") or "AWS 알림"
    if subject.startswith("AWS Budgets"):
        embed = budget_embed(sns["Message"], timestamp)
        if embed:
            return embed, True

    return {"title": subject, "description": sns["Message"], "color": GRAY, "timestamp": timestamp}, True


def post(embed, mention):
    embed["title"] = embed["title"][:TITLE_LIMIT]
    embed["description"] = embed.get("description", "")[:DESCRIPTION_LIMIT]
    embed = {key: value for key, value in embed.items() if value}

    role_id = os.environ.get("MENTION_ROLE_ID") if mention else None
    payload = {
        "content": f"<@&{role_id}>" if role_id else "",
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
