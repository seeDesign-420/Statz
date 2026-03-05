package com.statz.app.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility for converting between cents (Long) and display strings.
 * All money is stored as cents internally per spec §6.2.
 */
object MoneyUtils {

    private val zarLocale = Locale("en", "ZA")

    /**
     * Convert cents to display string: "R 1,250.00"
     */
    fun centsToDisplay(cents: Long): String {
        val rands = cents / 100.0
        val formatter = NumberFormat.getNumberInstance(zarLocale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "R ${formatter.format(rands)}"
    }

    /**
     * Parse a user input string to cents.
     * Accepts: "1250", "1250.00", "1,250.00", "R 1250", "R1250.50"
     * Returns null on invalid input.
     */
    fun parseToCents(input: String): Long? {
        val cleaned = input
            .replace("R", "")
            .replace(" ", "")
            .replace(",", "")
            .trim()

        if (cleaned.isEmpty()) return null

        return try {
            val value = cleaned.toDouble()
            if (value < 0) return null
            (value * 100).toLong()
        } catch (_: NumberFormatException) {
            null
        }
    }

    /**
     * Convert cents to rands as a Double (for calculations).
     */
    fun centsToRands(cents: Long): Double = cents / 100.0

    /**
     * Convert rands to cents.
     */
    fun randsToCents(rands: Double): Long = (rands * 100).toLong()
}
