package com.statz.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Work-hours-aware follow-up interval calculator.
 *
 * Default work hours: Mon–Fri 08:00–17:30.
 * All urgency intervals are calendar-time based.
 * CUSTOM urgency uses the provided customHours parameter.
 */
object WorkHoursUtils {

    val DEFAULT_WORK_START: LocalTime = LocalTime.of(8, 0)
    val DEFAULT_WORK_END: LocalTime = LocalTime.of(17, 30)
    val TIMEZONE: ZoneId = ZoneId.of("Africa/Johannesburg")

    /**
     * Calculate the next follow-up epoch millis from the given reference time.
     *
     * @param fromMillis The reference time in epoch millis.
     * @param urgency The urgency level of the query.
     * @param customHours Hours for CUSTOM urgency (ignored for other urgencies).
     * @param workStart Start of work hours (used for snapping results to work time).
     * @param workEnd End of work hours.
     * @param weekendsEnabled Whether work happens on weekends.
     * @return The next follow-up time in epoch millis.
     */
    fun calculateNextFollowUp(
        fromMillis: Long,
        urgency: QueryUrgency,
        customHours: Int? = null,
        workStart: LocalTime = DEFAULT_WORK_START,
        workEnd: LocalTime = DEFAULT_WORK_END,
        weekendsEnabled: Boolean = false
    ): Long {
        val from = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(fromMillis),
            TIMEZONE
        )

        val result = when (urgency) {
            QueryUrgency.LOW -> from.plusHours(48)
            QueryUrgency.MEDIUM -> from.plusHours(24)
            QueryUrgency.HIGH -> from.plusHours(12)
            QueryUrgency.CRITICAL -> from.plusHours(6)
            QueryUrgency.CUSTOM -> from.plusHours(customHours?.toLong() ?: 24)
        }

        // Snap to work hours if the result falls outside them
        val snapped = snapToNextWorkStart(result, workStart, workEnd, weekendsEnabled)

        return snapped.atZone(TIMEZONE).toInstant().toEpochMilli()
    }

    /**
     * If [dateTime] is outside work hours, snap forward to the next work start.
     * If within work hours, return as-is.
     */
    private fun snapToNextWorkStart(
        dateTime: LocalDateTime,
        workStart: LocalTime,
        workEnd: LocalTime,
        weekendsEnabled: Boolean
    ): LocalDateTime {
        var date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        // Check if it's a non-work day
        if (!isWorkDay(date, weekendsEnabled)) {
            date = nextWorkDay(date, weekendsEnabled)
            return LocalDateTime.of(date, workStart)
        }

        // Before work hours
        if (time.isBefore(workStart)) {
            return LocalDateTime.of(date, workStart)
        }

        // After work hours
        if (!time.isBefore(workEnd)) {
            date = nextWorkDay(date, weekendsEnabled)
            return LocalDateTime.of(date, workStart)
        }

        // Within work hours — no change
        return dateTime
    }

    private fun nextWorkDay(date: LocalDate, weekendsEnabled: Boolean): LocalDate {
        var next = date.plusDays(1)
        while (!isWorkDay(next, weekendsEnabled)) {
            next = next.plusDays(1)
        }
        return next
    }

    private fun isWorkDay(date: LocalDate, weekendsEnabled: Boolean): Boolean {
        if (weekendsEnabled) return true
        return date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
    }
}
