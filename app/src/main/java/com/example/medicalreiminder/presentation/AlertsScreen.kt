package com.example.medicalreiminder.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.medicalreiminder.viewModels.AlertViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    alertViewModel: AlertViewModel,
    onBack: () -> Unit
) {
    val alerts by alertViewModel.alerts.collectAsState()
    val errorMessage by alertViewModel.errorMessage.collectAsState()
    val isAlertsLoading by alertViewModel.isAlertsLoading.collectAsState()

    LaunchedEffect(Unit) {
        alertViewModel.startListening()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.alert_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LaunchedEffect(errorMessage) {
                    alertViewModel.clearError()
                }
            }

            if (isAlertsLoading) {
                LoadingAlerts()
            } else if (alerts.isEmpty()) {
                EmptyAlerts()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alerts, key = { it.alertId }) { alert ->
                        AlertCard(
                            alert = alert,
                            onMarkAsRead = { alertViewModel.markAsRead(alert.alertId) },
                            onMarkAsResolved = { alertViewModel.markAsResolved(alert.alertId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAlerts() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.loading_alerts))
    }
}

@Composable
private fun EmptyAlerts() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF607D8B)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.no_robot_alerts),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.no_robot_alerts_details),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    onMarkAsRead: () -> Unit,
    onMarkAsResolved: () -> Unit
) {
    val cardColor = when {
        alert.severity == "emergency" && !alert.isResolved -> Color(0xFFFFE7E7)
        !alert.isRead -> Color(0xFFFFF4E0)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title.ifBlank { stringResource(R.string.robot_alert) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    imageVector = if (alert.isResolved) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (alert.isResolved) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(alert.severity.ifBlank { "-" }) })
                AssistChip(onClick = {}, label = { Text(if (alert.isRead) stringResource(R.string.read) else stringResource(R.string.unread)) })
                AssistChip(onClick = {}, label = { Text(if (alert.isResolved) stringResource(R.string.resolved) else stringResource(R.string.unresolved)) })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.alert_type_value, alert.type.ifBlank { "-" }))
            Text(stringResource(R.string.robot_id_value, alert.robotId.ifBlank { "-" }))
            Text(stringResource(R.string.patient_room_value, alert.patientRoom.ifBlank { "-" }))
            Text(stringResource(R.string.timestamp_value, formatAlertTime(alert)))
            Text(stringResource(R.string.resolved_at_value, formatResolvedTime(alert)))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                if (!alert.isRead) {
                    OutlinedButton(onClick = onMarkAsRead) {
                        Text(stringResource(R.string.mark_as_read))
                    }
                }
                if (!alert.isResolved) {
                    Button(onClick = onMarkAsResolved) {
                        Text(stringResource(R.string.mark_as_resolved))
                    }
                }
            }
        }
    }
}

private fun formatAlertTime(alert: Alert): String {
    val date = alert.timestamp?.toDate() ?: return "-"
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date)
}

private fun formatResolvedTime(alert: Alert): String {
    val date = alert.resolvedAt?.toDate() ?: return "-"
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(date.time))
}
