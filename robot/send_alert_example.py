import os
import requests


FUNCTION_URL = os.environ["ROBOT_ALERT_FUNCTION_URL"]
ROBOT_SECRET = os.environ["ROBOT_ALERT_SECRET"]

payload = {
    "robotId": "robot_001",
    "title": "Medicine Reminder",
    "message": "It is time to take your medicine.",
    "type": "medicine_reminder",
}

response = requests.post(
    FUNCTION_URL,
    json=payload,
    headers={"Authorization": f"Bearer {ROBOT_SECRET}"},
    timeout=10,
)

response.raise_for_status()
print(response.json())
