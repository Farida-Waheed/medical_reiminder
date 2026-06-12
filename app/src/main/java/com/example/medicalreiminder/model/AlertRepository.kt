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
            .whereEqualTo("userId", userId)
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
                        robotId = document.getString("robotId").orEmpty(),
                        userId = document.getString("userId").orEmpty(),
                        timestamp = document.getTimestamp("timestamp"),
                        isRead = document.getBoolean("isRead") ?: false
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
