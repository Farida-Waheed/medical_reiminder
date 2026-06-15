import os
import json
import uuid
from datetime import datetime, timezone
from urllib.error import HTTPError
from urllib.request import Request, urlopen


FIREBASE_API_KEY = os.environ["FIREBASE_API_KEY"]
FIREBASE_PROJECT_ID = os.environ.get("FIREBASE_PROJECT_ID", "medical-reminder-g")
ROBOT_EMAIL = os.environ.get("ROBOT_EMAIL", "robot001@jalees.local")
ROBOT_PASSWORD = os.environ["ROBOT_PASSWORD"]
ROBOT_ID = os.environ.get("ROBOT_ID", "robot_001")


def request_json(method, url, data=None, headers=None, timeout=15):
    body = None
    request_headers = headers or {}
    if data is not None:
        body = json.dumps(data).encode("utf-8")
        request_headers = {
            **request_headers,
            "Content-Type": "application/json",
        }

    request = Request(
        url,
        data=body,
        headers=request_headers,
        method=method,
    )

    try:
        with urlopen(request, timeout=timeout) as response:
            response_body = response.read().decode("utf-8")
            return json.loads(response_body) if response_body else {}
    except HTTPError as error:
        error_body = error.read().decode("utf-8")
        raise RuntimeError(f"HTTP {error.code}: {error_body}") from error


def firestore_url(document_path):
    return (
        f"https://firestore.googleapis.com/v1/projects/{FIREBASE_PROJECT_ID}"
        f"/databases/(default)/documents/{document_path}"
    )


def sign_in_robot():
    response = request_json(
        "POST",
        f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={FIREBASE_API_KEY}",
        {
            "email": ROBOT_EMAIL,
            "password": ROBOT_PASSWORD,
            "returnSecureToken": True,
        },
    )
    return response["idToken"]


def auth_headers(id_token):
    return {"Authorization": f"Bearer {id_token}"}


def field(value):
    if value is None:
        return {"nullValue": None}
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    if isinstance(value, datetime):
        return {"timestampValue": value.isoformat().replace("+00:00", "Z")}
    return {"stringValue": str(value)}


def firestore_fields(data):
    return {"fields": {key: field(value) for key, value in data.items()}}


def get_robot(id_token):
    try:
        response = request_json(
            "GET",
            firestore_url(f"robots/{ROBOT_ID}"),
            headers=auth_headers(id_token),
        )
    except RuntimeError as error:
        raise RuntimeError(
            f"Could not read robots/{ROBOT_ID}. Create that Firestore document first "
            "and make sure its robotAuthUid equals this robot account UID."
        ) from error

    fields = response["fields"]
    return {
        "robotId": fields["robotId"]["stringValue"],
        "caregiverId": fields["caregiverId"]["stringValue"],
        "patientRoom": fields["patientRoom"]["stringValue"],
    }


def create_alert(id_token, robot):
    alert_id = str(uuid.uuid4())
    now = datetime.now(timezone.utc)
    alert = {
        "alertId": alert_id,
        "title": "Fall detected",
        "message": "The robot detected a possible patient fall in the monitored room.",
        "type": "fall_detected",
        "severity": "emergency",
        "robotId": robot["robotId"],
        "caregiverId": robot["caregiverId"],
        "patientRoom": robot["patientRoom"],
        "timestamp": now,
        "isRead": False,
        "isResolved": False,
        "resolvedAt": None,
    }

    request_json(
        "PATCH",
        firestore_url(f"alerts/{alert_id}"),
        headers=auth_headers(id_token),
        data=firestore_fields(alert),
    )
    return alert_id


def update_robot_status(id_token):
    now = datetime.now(timezone.utc)
    status = {
        "status": "online",
        "batteryLevel": 82,
        "lastSeen": now,
        "currentTask": "monitoring",
    }
    field_paths = "&".join(f"updateMask.fieldPaths={name}" for name in status.keys())

    request_json(
        "PATCH",
        f"{firestore_url(f'robots/{ROBOT_ID}')}?{field_paths}",
        headers=auth_headers(id_token),
        data=firestore_fields(status),
    )


def main():
    id_token = sign_in_robot()
    robot = get_robot(id_token)
    alert_id = create_alert(id_token, robot)
    update_robot_status(id_token)
    print({"alertId": alert_id, "robotId": ROBOT_ID})


if __name__ == "__main__":
    main()
