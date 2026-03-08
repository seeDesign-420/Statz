package com.statz.app.domain.model

import java.time.ZoneId

/**
 * Application-wide configuration constants.
 * Centralizes values that would otherwise be hardcoded across the codebase.
 */
object AppConfig {
    /** Default timezone used throughout the app for date/time calculations. */
    val TIMEZONE: ZoneId = ZoneId.of("Africa/Johannesburg")
}
