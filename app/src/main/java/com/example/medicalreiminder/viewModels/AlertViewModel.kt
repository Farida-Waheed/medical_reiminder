package com.example.medicalreiminder.viewModels

import androidx.lifecycle.ViewModel
import com.example.medicalreiminder.model.Alert
import com.example.medicalreiminder.model.AlertRepository
import com.example.medicalreiminder.model.RobotStatus
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertViewModel(
    private val repository: AlertRepository = AlertRepository()
) : ViewModel() {
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _robotStatus = MutableStateFlow<RobotStatus?>(null)
    val robotStatus: StateFlow<RobotStatus?> = _robotStatus.asStateFlow()

    private var alertListenerRegistration: ListenerRegistration? = null
    private var robotStatusListenerRegistration: ListenerRegistration? = null

    fun startListening() {
        if (alertListenerRegistration != null && robotStatusListenerRegistration != null) return

        repository.saveCurrentDeviceToken(::showError)
        if (alertListenerRegistration == null) {
            alertListenerRegistration = repository.listenToCurrentUserAlerts(
                onAlertsChanged = { _alerts.value = it },
                onError = ::showError
            )
        }
        if (robotStatusListenerRegistration == null) {
            robotStatusListenerRegistration = repository.listenToCurrentUserRobotStatus(
                onStatusChanged = { _robotStatus.value = it },
                onError = ::showError
            )
        }
    }

    fun markAsRead(alertId: String) {
        repository.markAsRead(alertId, ::showError)
    }

    fun markAsResolved(alertId: String) {
        repository.markAsResolved(alertId, ::showError)
    }

    fun saveDeviceToken() {
        repository.saveCurrentDeviceToken(::showError)
    }

    fun stopListening() {
        alertListenerRegistration?.remove()
        robotStatusListenerRegistration?.remove()
        alertListenerRegistration = null
        robotStatusListenerRegistration = null
        _alerts.value = emptyList()
        _robotStatus.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        alertListenerRegistration?.remove()
        robotStatusListenerRegistration?.remove()
        alertListenerRegistration = null
        robotStatusListenerRegistration = null
        super.onCleared()
    }

    private fun showError(message: String) {
        _errorMessage.value = message
    }
}
