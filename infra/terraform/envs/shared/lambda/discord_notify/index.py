import json
import os
import re
import urllib.parse
import urllib.request
from datetime import datetime

import boto3

RED = 0xE74C3C
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


# 경보 설명의 첫 줄은 제목, 나머지는 본문으로 쓴다
def alarm_embed(alarm):
    title, _, body = (alarm.get("AlarmDescription") or alarm["AlarmName"]).partition("\n")
    return {
        "title": title,
        "description": body,
        "url": alarm_console_url(alarm["AlarmName"]),
        "color": RED,
        "footer": {"text": alarm["AlarmName"]},
        "timestamp": iso_timestamp(alarm.get("StateChangeTime")),
    }


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


def to_embed(sns):
    timestamp = iso_timestamp(sns.get("Timestamp"))
    try:
        alarm = json.loads(sns["Message"])
    except json.JSONDecodeError:
        alarm = None

    if isinstance(alarm, dict) and "AlarmName" in alarm:
        return alarm_embed(alarm)

    subject = sns.get("Subject") or "AWS 알림"
    if subject.startswith("AWS Budgets"):
        embed = budget_embed(sns["Message"], timestamp)
        if embed:
            return embed

    return {"title": subject, "description": sns["Message"], "color": GRAY, "timestamp": timestamp}


def post(embed):
    embed["title"] = embed["title"][:TITLE_LIMIT]
    embed["description"] = embed.get("description", "")[:DESCRIPTION_LIMIT]
    embed = {key: value for key, value in embed.items() if value}

    role_id = os.environ.get("MENTION_ROLE_ID")
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
        post(to_embed(record["Sns"]))
