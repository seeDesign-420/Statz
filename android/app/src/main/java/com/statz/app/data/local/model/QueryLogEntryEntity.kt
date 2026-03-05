package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.statz.app.domain.model.QueryStatus

/**
 * Timestamped log entry for a query ticket.
 * Auto-logged on status changes; manually added notes.
 */
@Entity(
    tableName = "query_log_entries",
    foreignKeys = [
        ForeignKey(
            entity = QueryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["query_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("query_id")]
)
data class QueryLogEntryEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "query_id")
    val queryId: String,
    val timestamp: Long, // epoch millis
    val note: String,
    @ColumnInfo(name = "status_after")
    val statusAfter: QueryStatus? = null
)
