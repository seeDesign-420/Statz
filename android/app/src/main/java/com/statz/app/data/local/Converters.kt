package com.statz.app.data.local

import androidx.room.TypeConverter
import com.statz.app.domain.model.CategoryType
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency

/**
 * Room type converters for enum types.
 */
class Converters {

    // CategoryType
    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    // QueryStatus
    @TypeConverter
    fun fromQueryStatus(value: QueryStatus): String = value.name

    @TypeConverter
    fun toQueryStatus(value: String): QueryStatus = QueryStatus.valueOf(value)

    // QueryUrgency (used by both queries and tasks)
    @TypeConverter
    fun fromQueryUrgency(value: QueryUrgency): String = value.name

    @TypeConverter
    fun toQueryUrgency(value: String): QueryUrgency = QueryUrgency.valueOf(value)

    // QueryStatus? (nullable, for log entries)
    @TypeConverter
    fun fromNullableQueryStatus(value: QueryStatus?): String? = value?.name

    @TypeConverter
    fun toNullableQueryStatus(value: String?): QueryStatus? = value?.let { QueryStatus.valueOf(it) }
}

