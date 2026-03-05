package com.statz.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.statz.app.domain.model.CategoryType

/**
 * Seeded sales category definition.
 * id is a stable slug (e.g. "new", "upgrade", "accessories").
 */
@Entity(tableName = "sales_categories")
data class SalesCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: CategoryType,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true
)
