package com.statz.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App settings backed by Preferences DataStore.
 * Covers work hours, notification toggles per spec §5.1.
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "statz_settings")

data class AppSettings(
    val displayName: String = "",
    val workStartHour: Int = 8,
    val workStartMinute: Int = 0,
    val workEndHour: Int = 17,
    val workEndMinute: Int = 30,
    val weekendsEnabled: Boolean = false,
    val queryRemindersEnabled: Boolean = true,
    val todoRemindersEnabled: Boolean = true
) {
    val workStartTime: LocalTime get() = LocalTime.of(workStartHour, workStartMinute)
    val workEndTime: LocalTime get() = LocalTime.of(workEndHour, workEndMinute)
}

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val WORK_START_HOUR = intPreferencesKey("work_start_hour")
        val WORK_START_MINUTE = intPreferencesKey("work_start_minute")
        val WORK_END_HOUR = intPreferencesKey("work_end_hour")
        val WORK_END_MINUTE = intPreferencesKey("work_end_minute")
        val WEEKENDS_ENABLED = booleanPreferencesKey("weekends_enabled")
        val QUERY_REMINDERS = booleanPreferencesKey("query_reminders_enabled")
        val TODO_REMINDERS = booleanPreferencesKey("todo_reminders_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            workStartHour = prefs[Keys.WORK_START_HOUR] ?: 8,
            workStartMinute = prefs[Keys.WORK_START_MINUTE] ?: 0,
            workEndHour = prefs[Keys.WORK_END_HOUR] ?: 17,
            workEndMinute = prefs[Keys.WORK_END_MINUTE] ?: 30,
            weekendsEnabled = prefs[Keys.WEEKENDS_ENABLED] ?: false,
            queryRemindersEnabled = prefs[Keys.QUERY_REMINDERS] ?: true,
            todoRemindersEnabled = prefs[Keys.TODO_REMINDERS] ?: true
        )
    }

    suspend fun updateWorkHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WORK_START_HOUR] = startHour
            prefs[Keys.WORK_START_MINUTE] = startMinute
            prefs[Keys.WORK_END_HOUR] = endHour
            prefs[Keys.WORK_END_MINUTE] = endMinute
        }
    }

    suspend fun setWeekendsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WEEKENDS_ENABLED] = enabled }
    }

    suspend fun setQueryReminders(enabled: Boolean) {
        context.dataStore.edit { it[Keys.QUERY_REMINDERS] = enabled }
    }

    suspend fun setTodoReminders(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TODO_REMINDERS] = enabled }
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[Keys.DISPLAY_NAME] = name }
    }
}
