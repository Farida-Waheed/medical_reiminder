package com.example.medicalreiminder.model

import com.google.firebase.Timestamp

data class Alert(
    val alertId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val robotId: String = "",
    val userId: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false
)
