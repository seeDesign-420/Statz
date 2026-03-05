package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Monthly target per category.
 * Composite key: "${monthKey}_${categoryId}".
 * targetValue is units (for UNIT categories) or cents (for MONEY categories).
 */
@Entity(
    tableName = "monthly_targets",
    foreignKeys = [
        ForeignKey(
            entity = SalesCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("month_key"),
        Index("category_id")
    ]
)
data class MonthlyTargetEntity(
    @PrimaryKey
    val id: String, // "${monthKey}_${categoryId}"
    @ColumnInfo(name = "month_key")
    val monthKey: String, // YYYY-MM
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "target_value")
    val targetValue: Long, // units or cents
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long // epoch millis
)
