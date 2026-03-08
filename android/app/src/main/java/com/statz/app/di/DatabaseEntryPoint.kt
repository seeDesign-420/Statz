package com.statz.app.di

import com.statz.app.data.local.AppDatabase
import com.statz.app.data.local.dao.QueryDao
import com.statz.app.data.local.dao.TaskDao
import com.statz.app.notification.NotificationScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for accessing DI-provided dependencies from BroadcastReceivers.
 *
 * BroadcastReceivers are not Hilt-managed Android components, so they use
 * EntryPointAccessors to retrieve dependencies from the application's SingletonComponent.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DatabaseEntryPoint {
    fun appDatabase(): AppDatabase
    fun queryDao(): QueryDao
    fun taskDao(): TaskDao
    fun notificationScheduler(): NotificationScheduler
}
