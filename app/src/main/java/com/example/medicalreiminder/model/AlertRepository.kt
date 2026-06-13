package com.example.medicalreiminder.model

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging

class AlertRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {
    fun listenToCurrentUserAlerts(
        onAlertsChanged: (List<Alert>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onAlertsChanged(emptyList())
            return null
        }

        return firestore.collection("alerts")
            .whereEqualTo("caregiverId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Could not load alerts")
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents.orEmpty().map { document ->
                    Alert(
                        alertId = document.getString("alertId").orEmpty().ifBlank { document.id },
                        title = document.getString("title").orEmpty(),
                        message = document.getString("message").orEmpty(),
                        type = document.getString("type").orEmpty(),
                        severity = document.getString("severity").orEmpty(),
                        robotId = document.getString("robotId").orEmpty(),
                        caregiverId = document.getString("caregiverId").orEmpty(),
                        patientRoom = document.getString("patientRoom").orEmpty(),
                        timestamp = document.getTimestamp("timestamp"),
                        isRead = document.getBoolean("isRead") ?: false,
                        isResolved = document.getBoolean("isResolved") ?: false,
                        resolvedAt = document.getTimestamp("resolvedAt")
                    )
                }
                onAlertsChanged(alerts)
            }
    }

    fun markAsRead(alertId: String, onError: (String) -> Unit) {
        if (alertId.isBlank()) return

        firestore.collection("alerts")
            .document(alertId)
            .update("isRead", true)
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Could not mark alert as read")
            }
    }

    fun markAsResolved(alertId: String, onError: (String) -> Unit) {
        if (alertId.isBlank()) return

        firestore.collection("alerts")
            .document(alertId)
            .update(
                mapOf(
                    "isRead" to true,
                    "isResolved" to true,
                    "resolvedAt" to Timestamp.now()
                )
            )
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Could not mark alert as resolved")
            }
    }

    fun listenToCurrentUserRobotStatus(
        onStatusChanged: (RobotStatus?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val caregiverId = auth.currentUser?.uid
        if (caregiverId == null) {
            onStatusChanged(null)
            return null
        }

        return firestore.collection("robots")
            .whereEqualTo("caregiverId", caregiverId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Could not load robot status")
                    return@addSnapshotListener
                }

                val document = snapshot?.documents?.firstOrNull()
                if (document == null) {
                    onStatusChanged(null)
                    return@addSnapshotListener
                }

                onStatusChanged(
                    RobotStatus(
                        robotId = document.getString("robotId").orEmpty().ifBlank { document.id },
                        caregiverId = document.getString("caregiverId").orEmpty(),
                        patientRoom = document.getString("patientRoom").orEmpty(),
                        status = document.getString("status").orEmpty(),
                        batteryLevel = document.getLong("batteryLevel"),
                        lastSeen = document.getTimestamp("lastSeen"),
                        currentTask = document.getString("currentTask").orEmpty()
                    )
                )
            }
    }

    fun saveCurrentDeviceToken(onError: (String) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        messaging.token
            .addOnSuccessListener { token ->
                saveDeviceToken(userId, token, onError)
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Could not save notification token")
            }
    }

    fun saveDeviceToken(userId: String, token: String, onError: (String) -> Unit = {}) {
        if (userId.isBlank() || token.isBlank()) return

        val tokenData = mapOf(
            "token" to token,
            "createdAt" to Timestamp.now(),
            "platform" to "android"
        )

        firestore.collection("users")
            .document(userId)
            .collection("fcmTokens")
            .document(token)
            .set(tokenData)
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Could not save notification token")
            }
    }
}
