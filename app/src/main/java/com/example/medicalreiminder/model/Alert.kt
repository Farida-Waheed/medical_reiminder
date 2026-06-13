package com.example.medicalreiminder.model

import com.google.firebase.Timestamp

data class Alert(
    val alertId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val severity: String = "",
    val robotId: String = "",
    val caregiverId: String = "",
    val patientRoom: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val isResolved: Boolean = false,
    val resolvedAt: Timestamp? = null
)
