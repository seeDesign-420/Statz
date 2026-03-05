package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.statz.app.domain.model.QueryUrgency

/**
 * To-do task item with optional due date, urgency, and reminder.
 */
@Entity(tableName = "task_items")
data class TaskItemEntity(
    @PrimaryKey
    val id: String, // UUID
    val title: String,
    val notes: String? = null,
    @ColumnInfo(name = "due_at")
    val dueAt: Long? = null, // epoch millis
    val urgency: QueryUrgency,
    @ColumnInfo(name = "custom_follow_up_hours")
    val customFollowUpHours: Int? = null, // Only used when urgency == CUSTOM
    @ColumnInfo(name = "next_follow_up_at")
    val nextFollowUpAt: Long? = null, // epoch millis
    @ColumnInfo(name = "is_done", defaultValue = "0")
    val isDone: Boolean = false,
    @ColumnInfo(name = "reminder_enabled", defaultValue = "0")
    val reminderEnabled: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
