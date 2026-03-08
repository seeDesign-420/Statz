package com.statz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.data.local.model.QueryLogEntryEntity
import com.statz.app.domain.model.QueryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface QueryDao {

    // ── Query Items ─────────────────────────────────────────────

    @Insert
    suspend fun insertQuery(query: QueryItemEntity)

    @Update
    suspend fun updateQuery(query: QueryItemEntity)

    @Upsert
    suspend fun upsertQuery(query: QueryItemEntity)

    @Query("SELECT * FROM query_items WHERE id = :id")
    suspend fun getQueryById(id: String): QueryItemEntity?

    @Query("SELECT * FROM query_items WHERE id = :id")
    fun observeQueryById(id: String): Flow<QueryItemEntity?>

    /**
     * Observe all queries, sorted by urgency desc, nextFollowUpAt asc, updatedAt desc.
     * Spec §3.2.A sort order.
     */
    @Query("""
        SELECT * FROM query_items
        ORDER BY
            CASE urgency
                WHEN 'CRITICAL' THEN 0
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
            END ASC,
            next_follow_up_at ASC,
            updated_at DESC
    """)
    fun observeAllQueries(): Flow<List<QueryItemEntity>>

    /**
     * Observe queries filtered by status.
     */
    @Query("""
        SELECT * FROM query_items
        WHERE status = :status
        ORDER BY
            CASE urgency
                WHEN 'CRITICAL' THEN 0
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
            END ASC,
            next_follow_up_at ASC,
            updated_at DESC
    """)
    fun observeQueriesByStatus(status: QueryStatus): Flow<List<QueryItemEntity>>

    /**
     * Search queries by ticket number, customer ID, or customer name.
     */
    @Query("""
        SELECT * FROM query_items
        WHERE ticket_number LIKE '%' || :search || '%'
           OR customer_id LIKE '%' || :search || '%'
           OR customer_name LIKE '%' || :search || '%'
        ORDER BY
            CASE urgency
                WHEN 'CRITICAL' THEN 0
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
            END ASC,
            next_follow_up_at ASC,
            updated_at DESC
    """)
    fun searchQueries(search: String): Flow<List<QueryItemEntity>>

    /**
     * Find queries due for follow-up (for WorkManager reminder checks).
     * status != CLOSED and nextFollowUpAt <= now.
     */
    @Query("""
        SELECT * FROM query_items
        WHERE status != 'CLOSED'
          AND next_follow_up_at <= :nowMillis
    """)
    suspend fun getDueQueries(nowMillis: Long): List<QueryItemEntity>

    /**
     * Get all non-closed queries (for boot receiver rescheduling).
     */
    @Query("SELECT * FROM query_items WHERE status != 'CLOSED'")
    suspend fun getAllNonClosedQueries(): List<QueryItemEntity>

    @Query("DELETE FROM query_items WHERE id = :id")
    suspend fun deleteQuery(id: String)

    @Query("UPDATE query_items SET ticket_number = :ticketNumber, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTicketNumber(id: String, ticketNumber: String, updatedAt: Long)

    // ── Log Entries ─────────────────────────────────────────────

    @Insert
    suspend fun insertLogEntry(entry: QueryLogEntryEntity)

    @Query("SELECT * FROM query_log_entries WHERE query_id = :queryId ORDER BY timestamp DESC")
    fun observeLogEntries(queryId: String): Flow<List<QueryLogEntryEntity>>

    @Query("SELECT * FROM query_log_entries WHERE query_id = :queryId ORDER BY timestamp DESC")
    suspend fun getLogEntries(queryId: String): List<QueryLogEntryEntity>
}
