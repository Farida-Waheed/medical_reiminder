# Jalees Robot Integration

The active production architecture is Spark-plan only:

```text
Robot -> Firebase Auth -> Firestore -> Caregiver App
```

Do not use Cloud Functions, Admin SDK, service accounts, or Blaze-only Firebase services.

## Firebase Setup

1. Enable Firebase Authentication Email/Password.
2. Create one robot auth account:

```text
robot001@jalees.local
```

3. Copy the robot user's Firebase Auth UID.
4. Create one Firestore robot document manually from Firebase Console.

Path:

```text
robots/robot_001
```

Fields:

```json
{
  "robotId": "robot_001",
  "caregiverId": "<caregiver Firebase Auth UID>",
  "robotAuthUid": "<robot Firebase Auth UID>",
  "patientRoom": "Room 101",
  "status": "online",
  "batteryLevel": 82,
  "lastSeen": "<Firestore timestamp>",
  "currentTask": "monitoring"
}
```

If the robot script gets `HTTP 403: Missing or insufficient permissions` while reading `robots/robot_001`, the robot document is missing or `robotAuthUid` does not match the signed-in robot account.

## Robot Code Changes

The real robot code needs to do these steps whenever it sends data:

1. Sign in with Firebase Auth REST API using robot email/password.
2. Save the returned `idToken` in memory.
3. Read `robots/{robotId}` using Firestore REST API.
4. Create `alerts/{alertId}` using Firestore REST API when a real emergency event happens.
5. Update `robots/{robotId}` status fields using Firestore REST API.

Use this endpoint to sign in:

```text
POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=<FIREBASE_API_KEY>
```

Body:

```json
{
  "email": "robot001@jalees.local",
  "password": "<robot password>",
  "returnSecureToken": true
}
```

Use the returned token in Firestore requests:

```text
Authorization: Bearer <idToken>
```

## Alert Document Contract

Path:

```text
alerts/{alertId}
```

Required fields:

```json
{
  "alertId": "<same as document id>",
  "title": "Fall detected",
  "message": "The robot detected a possible patient fall in the monitored room.",
  "type": "fall_detected",
  "severity": "emergency",
  "robotId": "robot_001",
  "caregiverId": "<from robots/robot_001>",
  "patientRoom": "<from robots/robot_001>",
  "timestamp": "<server/client timestamp>",
  "isRead": false,
  "isResolved": false,
  "resolvedAt": null
}
```

Allowed alert `type` values:

```text
fall_detected
unknown_person_detected
patient_not_detected
robot_disconnected
robot_battery_low
emergency
```

Allowed `severity` values:

```text
info
warning
emergency
```

## Robot Status Contract

Path:

```text
robots/robot_001
```

The robot may update only these fields:

```json
{
  "status": "online",
  "batteryLevel": 82,
  "lastSeen": "<timestamp>",
  "currentTask": "monitoring"
}
```

The robot must not change:

```text
robotId
caregiverId
robotAuthUid
patientRoom
```

## Local Test Script

The sample script is:

```text
robot/send_alert_example.py
```

It uses only Python standard library modules.

Run it with:

```powershell
$env:FIREBASE_API_KEY="<web api key from google-services.json>"
$env:FIREBASE_PROJECT_ID="medical-reminder-g"
$env:ROBOT_EMAIL="robot001@jalees.local"
$env:ROBOT_PASSWORD="<robot password>"
$env:ROBOT_ID="robot_001"

python robot\send_alert_example.py
```

Expected output:

```text
{'alertId': '...', 'robotId': 'robot_001'}
```

## Caregiver App Behavior

The app listens to:

```text
alerts where caregiverId == current caregiver uid ordered by timestamp desc
robots where caregiverId == current caregiver uid
```

The caregiver can mark alerts as read/resolved. The caregiver app does not create alerts and does not control the robot.

Spark-plan limitation: closed-app push notifications are not available without a trusted sender such as Cloud Functions or another FCM backend. The app will receive Firestore realtime updates while running.
