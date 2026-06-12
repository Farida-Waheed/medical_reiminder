package com.example.medicalreiminder.viewModels

import androidx.lifecycle.ViewModel
import com.example.medicalreiminder.model.Alert
import com.example.medicalreiminder.model.AlertRepository
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

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening() {
        if (listenerRegistration != null) return

        repository.saveCurrentDeviceToken(::showError)
        listenerRegistration = repository.listenToCurrentUserAlerts(
            onAlertsChanged = { _alerts.value = it },
            onError = ::showError
        )
    }

    fun markAsRead(alertId: String) {
        repository.markAsRead(alertId, ::showError)
    }

    fun saveDeviceToken() {
        repository.saveCurrentDeviceToken(::showError)
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
        _alerts.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        listenerRegistration = null
        super.onCleared()
    }

    private fun showError(message: String) {
        _errorMessage.value = message
    }
}
