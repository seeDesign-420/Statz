package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily sales record for open orders and declined counts per day.
 * Category-specific values are in DailySalesValueEntity.
 */
@Entity(
    tableName = "daily_sales_records",
    indices = [Index("month_key")]
)
data class DailySalesRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "date_key")
    val dateKey: String, // YYYY-MM-DD
    @ColumnInfo(name = "month_key")
    val monthKey: String, // YYYY-MM — derived but stored for query efficiency
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long, // epoch millis
    @ColumnInfo(name = "open_orders_new", defaultValue = "0")
    val openOrdersNew: Int = 0,
    @ColumnInfo(name = "open_orders_upgrade", defaultValue = "0")
    val openOrdersUpgrade: Int = 0,
    @ColumnInfo(name = "declined_new", defaultValue = "0")
    val declinedNew: Int = 0,
    @ColumnInfo(name = "declined_upgrade", defaultValue = "0")
    val declinedUpgrade: Int = 0
)
