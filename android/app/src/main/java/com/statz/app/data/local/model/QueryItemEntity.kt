package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency

/**
 * Customer query ticket for follow-up tracking.
 */
@Entity(
    tableName = "query_items"
)
data class QueryItemEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "ticket_number")
    val ticketNumber: String,
    @ColumnInfo(name = "customer_id")
    val customerId: String,
    @ColumnInfo(name = "customer_name")
    val customerName: String,
    val status: QueryStatus,
    val urgency: QueryUrgency,
    @ColumnInfo(name = "custom_follow_up_hours")
    val customFollowUpHours: Int? = null, // Only used when urgency == CUSTOM
    @ColumnInfo(name = "next_follow_up_at")
    val nextFollowUpAt: Long, // epoch millis
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null
)
