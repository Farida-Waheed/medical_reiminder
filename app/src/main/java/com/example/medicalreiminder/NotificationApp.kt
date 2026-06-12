package com.example.medicalreiminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class NotificationApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(
            "medicine_reminder",
            "Medicine reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "Your medicine reminders"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val robotAlertChannel = NotificationChannel(
            RobotAlertMessagingService.ROBOT_ALERT_CHANNEL_ID,
            "Robot alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        robotAlertChannel.description = "Alerts sent by the robot"
        notificationManager.createNotificationChannel(robotAlertChannel)
    }
}
