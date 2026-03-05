package com.statz.app.di

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import com.statz.app.data.local.AppDatabase
import com.statz.app.data.local.dao.QueryDao
import com.statz.app.data.local.dao.SalesDao
import com.statz.app.data.local.dao.TaskDao
import com.statz.app.notification.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        lateinit var db: AppDatabase
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .addCallback(AppDatabase.createSeedCallback { db })
            .build()
        // Share instance with BroadcastReceivers
        AppDatabase.setInstance(db)
        return db
    }

    @Provides
    fun provideSalesDao(database: AppDatabase): SalesDao = database.salesDao()

    @Provides
    fun provideQueryDao(database: AppDatabase): QueryDao = database.queryDao()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager
    ): NotificationScheduler = NotificationScheduler(context, alarmManager)
}
