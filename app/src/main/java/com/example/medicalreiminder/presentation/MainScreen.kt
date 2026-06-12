package com.example.medicalreiminder.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalreiminder.R
import com.example.medicalreiminder.cancelAlarm
import com.example.medicalreiminder.viewModels.AuthenticationViewModel
import com.example.medicalreiminder.viewModels.AlertViewModel
import com.example.medicalreiminder.viewModels.ReminderViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.medicalreiminder.model.utils.checkGpsState
import com.example.medicalreiminder.model.utils.getCurrentLocation
import com.example.medicalreiminder.model.utils.hasPermission
import com.example.medicalreiminder.model.utils.sendEmergencyMessage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier,
    ReminderViewModel: ReminderViewModel,
    authenticationViewModel: AuthenticationViewModel,
    alertViewModel: AlertViewModel,
    onAddMed: (String, Long, Long, String) -> Unit,
    onEditMed: (Int, String, Long, Long, String) -> Unit,
    onLogout: () -> Unit,
    onAlerts: () -> Unit
) {
    val medications = ReminderViewModel.reminders.collectAsState(emptyList()).value.toMutableList()
    val context = LocalContext.current

    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val permission = android.Manifest.permission.POST_NOTIFICATIONS
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    val shouldRequestPermission = remember { mutableStateOf(false) }
    val requestLocationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted && checkGpsState(context)) {
                    getCurrentLocation(context) { lat, long ->
                        sendEmergencyMessage(context, lat, long)
                    }
                }
            }
        )
    val requestNotificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (!isGranted) {
                    showPermissionDeniedDialog = true
                }
                }
        )
    val isGranted = ContextCompat.checkSelfPermission(
        context, permission
    ) == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(Unit) {
        alertViewModel.saveDeviceToken()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted) {
            shouldRequestPermission.value = true
        }
    }

    if (shouldRequestPermission.value) {
        LaunchedEffect(shouldRequestPermission.value) {
            requestNotificationPermissionLauncher.launch(permission)
            shouldRequestPermission.value = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background
        if (showPermissionDeniedDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPermissionDeniedDialog = false },
                title = { Text("Notification Permission Required") },
                text = {
                    Text("This app requires notification permissions to remind you about your medications. Please enable it from settings.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDeniedDialog = false
                            // Optional: Open app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    Button(onClick = { showPermissionDeniedDialog = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }

        Image(
            painter = painterResource(id = R.drawable.backg),
            contentDescription = "Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Overflow menu (three dots)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Black)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Robot Alerts") },
                    onClick = {
                        menuExpanded = false
                        onAlerts()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        menuExpanded = false
                        alertViewModel.stopListening()
                       authenticationViewModel.logOut()
                        onLogout()
                    }
                )
            }
        }

        if (medications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Add your first Medication to get a Reminder",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28125B),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onAddMed("", 0L, 0L, "") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF77AADA)),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)
                        .width(200.dp)
                ) {
                    Text("Add your Med", fontSize = 16.sp, color = Color.White)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(top=40.dp)) {
                items(medications) { med ->
                    RemiderCard(
                        reminder = med,
                        onDelete = {
                            ReminderViewModel.deleteReminder(med)
                            authenticationViewModel.deleteReminderFromFireBase(med)
                            cancelAlarm(context, med)
                        }) {
                        onEditMed(
                            med.id,
                            med.name,
                            med.time,
                            med.timeOffset,
                            med.dose
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = { onAddMed("", 0L, 0L, "") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                containerColor = Color(0xFF77AADA)
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }

        // SOS Button
        FloatingActionButton(
            onClick = {
                if (hasPermission(context)) {
                    if (checkGpsState(context)) {
                    //Toast.makeText(context, "has gps", Toast.LENGTH_SHORT).show()
                        getCurrentLocation(context) { lat, long ->
                            sendEmergencyMessage(context, lat, long)
                        }
                    } else {
                        Toast.makeText(context, "Please turn GPS on", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp),
            shape = CircleShape,
            containerColor = Color.Red
        ) {
            Text("SOS", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        // "+" Add Med Button

    }
}
