package com.statz.app.domain.export

import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.data.repository.CategoryDashboard
import com.statz.app.data.repository.DailyEntry
import com.statz.app.domain.model.CategoryType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formats a daily sales report string for clipboard export.
 * Output matches the exact template structure the user expects.
 */
object ClipboardExporter {

    private val exportDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")

    // Categories that get combined into "Home Wifi" in the MTD section
    private val HOME_WIFI_IDS = setOf("home_wifi_contract", "home_wifi_mtm")

    // Ordered list of category IDs for the MTD section (excluding home wifi which is combined)
    private val MTD_UNIT_ORDER = listOf(
        "new", "upgrade", "sme_new", "sme_up", "ec_new", "ec_upgd"
    )

    // Ordered list for daily sales section (all individual categories)
    private val DAILY_ORDER = listOf(
        "new", "upgrade", "sme_new", "sme_up", "ec_new", "ec_upgd",
        "fiber", "home_wifi_contract", "home_wifi_mtm",
        "accessories", "insurance", "cash_sales"
    )

    /**
     * Build the full clipboard report text.
     */
    fun formatDailyReport(
        displayName: String,
        dateKey: String,
        monthlyCategories: List<CategoryDashboard>,
        dailyEntry: DailyEntry
    ): String {
        val date = LocalDate.parse(dateKey)
        val dateDisplay = date.format(exportDateFormatter)
        val catMap = monthlyCategories.associateBy { it.category.id }

        return buildString {
            appendLine(displayName)
            appendLine("[Date: $dateDisplay]")
            appendLine()

            // ── MTD Section (Target/Actual) ─────────────────────
            // Core unit categories
            MTD_UNIT_ORDER.forEach { id ->
                val cat = catMap[id] ?: return@forEach
                appendLine("${getMtdLabel(id)} = T${cat.target}/A${cat.actual}")
            }

            // Combined Home Wifi line
            val wifiCats = HOME_WIFI_IDS.mapNotNull { catMap[it] }
            if (wifiCats.isNotEmpty()) {
                val wifiActual = wifiCats.sumOf { it.actual }
                val wifiTarget = wifiCats.sumOf { it.target }
                appendLine("Home Wifi = T${wifiTarget}/A${wifiActual}")
            }

            appendLine()

            // Accessories (money) + Insurance (unit) — grouped together in template
            catMap["accessories"]?.let { cat ->
                appendLine("Accessories = T${cat.target}/R${cat.actual}")
            }
            catMap["insurance"]?.let { cat ->
                appendLine("Insurance = T${cat.target}/A${cat.actual}")
            }

            appendLine()

            // ── Daily Sales Section ─────────────────────────────
            appendLine("Daily sales")
            DAILY_ORDER.forEach { id ->
                val cat = catMap[id] ?: return@forEach
                val value = dailyEntry.categoryValues[id] ?: 0L
                val isMoney = cat.category.type == CategoryType.MONEY
                val prefix = if (isMoney) "R" else ""
                appendLine("${cat.category.name} = ${prefix}$value")
            }

            appendLine()

            // ── Open Orders ─────────────────────────────────────
            appendLine("Open orders")
            appendLine("New = ${dailyEntry.openOrdersNew}")
            appendLine("Upgrade = ${dailyEntry.openOrdersUpgrade}")

            appendLine()

            // ── Declined ────────────────────────────────────────
            appendLine("Declined")
            appendLine("New = ${dailyEntry.declinedNew}")
            append("Upgrade = ${dailyEntry.declinedUpgrade}")
        }
    }

    /** Maps category IDs to the MTD display labels. */
    private fun getMtdLabel(categoryId: String): String {
        return when (categoryId) {
            "new" -> "New line"
            "upgrade" -> "Upgrade"
            "sme_new" -> "Sme New"
            "sme_up" -> "Sme Up"
            "ec_new" -> "Employee Connect New"
            "ec_upgd" -> "Employee Connect Upgd"
            else -> categoryId
        }
    }
}
