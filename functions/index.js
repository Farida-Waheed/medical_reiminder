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

    const { robotId, title, message, type } = req.body || {};
    if (!robotId || !title || !message || !type) {
      res.status(400).json({
        error: "robotId, title, message, and type are required"
      });
      return;
    }

    const robotSnapshot = await db.collection("robots").doc(robotId).get();
    if (!robotSnapshot.exists) {
      res.status(404).json({ error: "Robot not found" });
      return;
    }

    const robot = robotSnapshot.data();
    const userId = robot.ownerUserId;
    if (!userId) {
      res.status(400).json({ error: "Robot has no ownerUserId" });
      return;
    }

    const alertRef = db.collection("alerts").doc();
    const alert = {
      alertId: alertRef.id,
      title,
      message,
      type,
      robotId,
      userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isRead: false
    };

    await alertRef.set(alert);

    const tokensSnapshot = await db
      .collection("users")
      .doc(userId)
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
