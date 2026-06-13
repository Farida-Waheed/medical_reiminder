const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();

exports.createRobotAlert = onRequest(async (req, res) => {
  try {
    if (req.method !== "POST") {
      res.status(405).json({ error: "Use POST" });
      return;
    }

    const expectedSecret = process.env.ROBOT_ALERT_SECRET;
    const authHeader = req.get("authorization") || "";
    if (!expectedSecret || authHeader !== `Bearer ${expectedSecret}`) {
      res.status(401).json({ error: "Unauthorized robot request" });
      return;
    }

    const { robotId, title, message, type, severity } = req.body || {};
    if (!robotId || !title || !message || !type || !severity) {
      res.status(400).json({
        error: "robotId, title, message, type, and severity are required"
      });
      return;
    }

    const allowedTypes = [
      "fall_detected",
      "unknown_person_detected",
      "patient_not_detected",
      "robot_disconnected",
      "robot_battery_low",
      "emergency"
    ];
    const allowedSeverities = ["info", "warning", "emergency"];
    if (!allowedTypes.includes(type) || !allowedSeverities.includes(severity)) {
      res.status(400).json({ error: "Invalid alert type or severity" });
      return;
    }

    const robotSnapshot = await db.collection("robots").doc(robotId).get();
    if (!robotSnapshot.exists) {
      res.status(404).json({ error: "Robot not found" });
      return;
    }

    const robot = robotSnapshot.data();
    const caregiverId = robot.caregiverId;
    const patientRoom = robot.patientRoom || req.body.patientRoom || "";
    if (!caregiverId) {
      res.status(400).json({ error: "Robot has no caregiverId" });
      return;
    }

    const alertRef = db.collection("alerts").doc();
    const alert = {
      alertId: alertRef.id,
      title,
      message,
      type,
      severity,
      robotId,
      caregiverId,
      patientRoom,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isRead: false,
      isResolved: false,
      resolvedAt: null
    };

    await alertRef.set(alert);

    const tokensSnapshot = await db
      .collection("users")
      .doc(caregiverId)
      .collection("fcmTokens")
      .get();

    const tokens = tokensSnapshot.docs
      .map((doc) => doc.data().token)
      .filter(Boolean);

    if (tokens.length > 0) {
      await admin.messaging().sendEachForMulticast({
        tokens,
        notification: {
          title,
          body: message
        },
        data: {
          alertId: alertRef.id,
          type,
          severity,
          robotId
        }
      });
    }

    res.status(201).json({ alertId: alertRef.id });
  } catch (error) {
    console.error("createRobotAlert failed", error);
    res.status(500).json({ error: "Could not create alert" });
  }
});
