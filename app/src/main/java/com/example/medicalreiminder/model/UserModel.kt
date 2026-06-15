package com.example.medicalreiminder.model

data class UserModel(
    val uid: String,
    val name: String,
    val email: String,
    val role: String = "caregiver"
)
