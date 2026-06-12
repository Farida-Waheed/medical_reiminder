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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicalreiminder.model.Alert
import com.example.medicalreiminder.viewModels.AlertViewModel
import java.text.SimpleDateFormat
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

    LaunchedEffect(Unit) {
        alertViewModel.startListening()
        alertViewModel.saveDeviceToken()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Robot Alerts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            if (alerts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF77AADA)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No robot alerts yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "New alerts from the robot will appear here.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alerts, key = { it.alertId }) { alert ->
                        AlertCard(
                            alert = alert,
                            onMarkAsRead = { alertViewModel.markAsRead(alert.alertId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    onMarkAsRead: () -> Unit
) {
    val cardColor = if (alert.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFEAF4FF)
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
                        text = alert.title.ifBlank { "Robot Alert" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (alert.isRead) "Read" else "Unread") },
                    leadingIcon = {
                        if (alert.isRead) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${alert.type.ifBlank { "general" }} - Robot ${alert.robotId.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatAlertTime(alert),
                style = MaterialTheme.typography.bodySmall
            )

            if (!alert.isRead) {
                Button(
                    onClick = onMarkAsRead,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Mark as read")
                }
            }
        }
    }
}

private fun formatAlertTime(alert: Alert): String {
    val date = alert.timestamp?.toDate() ?: return "No timestamp"
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date)
}
