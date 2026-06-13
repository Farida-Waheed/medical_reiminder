package com.example.medicalreiminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class NotificationApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val robotAlertChannel = NotificationChannel(
            RobotAlertMessagingService.ROBOT_ALERT_CHANNEL_ID,
            "Robot emergency alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        robotAlertChannel.description = "Emergency alerts sent by the robot"
        notificationManager.createNotificationChannel(robotAlertChannel)
    }
}
