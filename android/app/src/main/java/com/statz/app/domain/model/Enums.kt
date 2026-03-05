package com.statz.app.domain.model

/**
 * Type of sales category — either a unit count or a monetary value.
 */
enum class CategoryType {
    UNIT,
    MONEY
}

/**
 * Lifecycle status of a customer query ticket.
 */
enum class QueryStatus {
    OPEN,
    FOLLOW_UP,
    ESCALATED,
    CLOSED
}

/**
 * Urgency level for query follow-up scheduling.
 * Also used by to-do tasks for unified urgency handling.
 */
enum class QueryUrgency {
    LOW,        // 48 hours
    MEDIUM,     // 24 hours
    HIGH,       // 12 hours
    CRITICAL,   // 6 hours
    CUSTOM      // User-defined interval
}
