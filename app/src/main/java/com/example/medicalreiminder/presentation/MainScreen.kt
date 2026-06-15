package com.example.medicalreiminder.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicalreiminder.R
import com.example.medicalreiminder.model.Alert
import com.example.medicalreiminder.model.RobotStatus
import com.example.medicalreiminder.viewModels.AlertViewModel
import com.example.medicalreiminder.viewModels.AuthenticationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier,
    authenticationViewModel: AuthenticationViewModel,
    alertViewModel: AlertViewModel,
    onLogout: () -> Unit,
    onAlerts: () -> Unit
) {
    val alerts by alertViewModel.alerts.collectAsState()
    val robotStatus by alertViewModel.robotStatus.collectAsState()
    val errorMessage by alertViewModel.errorMessage.collectAsState()
    val isAlertsLoading by alertViewModel.isAlertsLoading.collectAsState()
    val isRobotStatusLoading by alertViewModel.isRobotStatusLoading.collectAsState()
    val latestUnreadAlert = alerts.firstOrNull { !it.isRead }
    val unresolvedEmergencyCount = alerts.count {
        it.severity == "emergency" && !it.isResolved
    }
    val unreadCount = alerts.count { !it.isRead }

    LaunchedEffect(Unit) {
        alertViewModel.startListening()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(
                        onClick = {
                            alertViewModel.stopListening()
                            authenticationViewModel.logOut()
                            onLogout()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmergencySummaryCard(
                unresolvedEmergencyCount = unresolvedEmergencyCount,
                unreadCount = unreadCount
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            LatestUnreadAlertCard(
                alert = latestUnreadAlert,
                isLoading = isAlertsLoading,
                onOpenAlerts = onAlerts
            )
            RobotStatusCard(
                robotStatus = robotStatus,
                isLoading = isRobotStatusLoading
            )
            Button(
                onClick = onAlerts,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text(
                    text = stringResource(R.string.open_alert_history),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmergencySummaryCard(
    unresolvedEmergencyCount: Int,
    unreadCount: Int
) {
    val hasEmergency = unresolvedEmergencyCount > 0
    val color = if (hasEmergency) Color(0xFFFFE7E7) else Color(0xFFEAF7EE)
    val title = if (hasEmergency) {
        stringResource(R.string.emergency_active)
    } else {
        stringResource(R.string.emergency_stable)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasEmergency) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.unresolved_emergencies, unresolvedEmergencyCount))
                Text(stringResource(R.string.unread_alerts, unreadCount))
            }
        }
    }
}

@Composable
private fun LatestUnreadAlertCard(
    alert: Alert?,
    isLoading: Boolean,
    onOpenAlerts: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.latest_unread_alert),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = stringResource(R.string.loading_alerts),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else if (alert == null) {
                Text(stringResource(R.string.no_unread_alerts))
            } else {
                Text(alert.title.ifBlank { stringResource(R.string.robot_alert) }, fontWeight = FontWeight.Bold)
                Text(alert.message)
                Text(stringResource(R.string.severity_value, alert.severity.ifBlank { "-" }))
                Text(stringResource(R.string.patient_room_value, alert.patientRoom.ifBlank { "-" }))
                Text(formatTimestamp(alert.timestamp?.toDate()?.time))
                Button(
                    onClick = onOpenAlerts,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.view_details))
                }
            }
        }
    }
}

@Composable
private fun RobotStatusCard(
    robotStatus: RobotStatus?,
    isLoading: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.robot_status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = stringResource(R.string.loading_robot_status),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                robotStatus == null -> Text(stringResource(R.string.no_robot_status))

                else -> {
                    Text(stringResource(R.string.robot_id_value, robotStatus.robotId.ifBlank { "-" }))
                    Text(stringResource(R.string.status_value, robotStatus.status.ifBlank { "-" }))
                    Text(stringResource(R.string.battery_value, robotStatus.batteryLevel?.toString() ?: "-"))
                    Text(stringResource(R.string.current_task_value, robotStatus.currentTask.ifBlank { "-" }))
                    Text(stringResource(R.string.patient_room_value, robotStatus.patientRoom.ifBlank { "-" }))
                    Text(
                        stringResource(
                            R.string.last_seen_value,
                            formatTimestamp(robotStatus.lastSeen?.toDate()?.time)
                        )
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timeMillis: Long?): String {
    if (timeMillis == null) return "-"
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(timeMillis))
}
