package com.example.medicalreiminder.model

import com.google.firebase.Timestamp

data class RobotStatus(
    val robotId: String = "",
    val caregiverId: String = "",
    val patientRoom: String = "",
    val status: String = "",
    val batteryLevel: Long? = null,
    val lastSeen: Timestamp? = null,
    val currentTask: String = ""
)
