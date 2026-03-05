package com.statz.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statz.app.data.backup.BackupManager
import com.statz.app.data.settings.AppSettings
import com.statz.app.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastResult: String? = null // success/error message
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val backupManager: BackupManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    // ── Work Hours ──────────────────────────────────────────────

    fun updateWorkHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            settingsDataStore.updateWorkHours(startHour, startMinute, endHour, endMinute)
        }
    }

    fun setWeekendsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setWeekendsEnabled(enabled) }
    }

    // ── Profile ──────────────────────────────────────────────────

    fun setDisplayName(name: String) {
        viewModelScope.launch { settingsDataStore.setDisplayName(name) }
    }

    // ── Notifications ───────────────────────────────────────────

    fun setQueryReminders(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setQueryReminders(enabled) }
    }

    fun setTodoReminders(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setTodoReminders(enabled) }
    }

    // ── Backup ──────────────────────────────────────────────────

    fun exportBackup(uri: Uri) {
        _backupState.value = BackupUiState(isExporting = true)
        viewModelScope.launch {
            val result = backupManager.exportToUri(uri)
            _backupState.value = BackupUiState(
                lastResult = if (result.isSuccess) "Export successful" else "Export failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun importBackup(uri: Uri) {
        _backupState.value = BackupUiState(isImporting = true)
        viewModelScope.launch {
            val result = backupManager.importFromUri(uri)
            _backupState.value = BackupUiState(
                lastResult = if (result.isSuccess) "Import successful — restart recommended" else "Import failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearBackupResult() {
        _backupState.value = BackupUiState()
    }
}
