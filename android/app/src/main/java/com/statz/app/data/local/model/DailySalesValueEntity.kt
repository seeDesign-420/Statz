package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Individual category value for a given day.
 * Normalized (not JSON-in-DB) for efficient sum queries.
 * value is units for UNIT categories, cents for MONEY categories.
 */
@Entity(
    tableName = "daily_sales_values",
    foreignKeys = [
        ForeignKey(
            entity = DailySalesRecordEntity::class,
            parentColumns = ["date_key"],
            childColumns = ["date_key"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SalesCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("date_key"),
        Index("category_id"),
        Index(value = ["date_key", "category_id"], unique = true)
    ]
)
data class DailySalesValueEntity(
    @PrimaryKey
    val id: String, // "${dateKey}_${categoryId}"
    @ColumnInfo(name = "date_key")
    val dateKey: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    val value: Long // units or cents
)
