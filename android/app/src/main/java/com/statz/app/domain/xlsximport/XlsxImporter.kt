package com.statz.app.domain.xlsximport

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of parsing an XLSX SalesTracker file.
 */
data class XlsxImportResult(
    val monthKey: String,                              // "2026-03"
    val salesPersonName: String,                       // "Thomas Lee"
    val targets: Map<String, Long>,                    // categoryId → target value (units or cents)
    val dailyEntries: Map<String, Map<String, Long>>,  // dateKey → (categoryId → value)
    val skippedProducts: List<String>                   // product names with no matching category
)

/**
 * Maps XLSX product row names to app category IDs.
 * Returns null for products that should be skipped.
 */
private val PRODUCT_TO_CATEGORY: Map<String, ProductMapping> = mapOf(
    "New lines"         to ProductMapping("new", false),
    "Upgrades"          to ProductMapping("upgrade", false),
    "Accessory R Value" to ProductMapping("accessories", true),  // Rands → cents
    "Insurance"         to ProductMapping("insurance", false),
    "Fibre"             to ProductMapping("fiber", false),
    "SME new"           to ProductMapping("sme_new", false),
    "SME upgrade"       to ProductMapping("sme_up", false),
    "FWA"               to ProductMapping("home_wifi_contract", false)
)

/**
 * Mapping metadata for an XLSX product row.
 * @param categoryId The Room category ID to map to.
 * @param isMoney Whether the value represents money (Rands) that needs ×100 conversion to cents.
 */
private data class ProductMapping(
    val categoryId: String,
    val isMoney: Boolean
)

/**
 * Month name → Month ordinal lookup (case-insensitive, supports full and abbreviated names).
 */
private val MONTH_NAMES: Map<String, Int> = buildMap {
    for (m in Month.entries) {
        put(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase(), m.value)
        put(m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).lowercase(), m.value)
    }
}

/**
 * Parses MTN SalesTracker XLSX files and extracts daily sales data + monthly targets.
 *
 * Expected XLSX structure:
 * - Row 18: "Sales for <Name>"
 * - Row 19: Headers (Product, Target, Total, ..., 1, 2, 3, ..., 31)
 * - Rows 20-31: Per-product data grids
 *
 * Month/year is extracted from the filename pattern:
 * `SalesTracker_..._<Month>_<Year>.xlsx`
 */
@Singleton
class XlsxImporter @Inject constructor() {

    /**
     * Parse an XLSX SalesTracker file.
     * @param inputStream The file contents.
     * @param filename The original filename (used to extract month/year).
     * @return Parsed import result.
     * @throws IllegalArgumentException if the file structure is unrecognized.
     */
    fun parse(inputStream: InputStream, filename: String): XlsxImportResult {
        val monthKey = extractMonthKey(filename)
        val yearMonth = YearMonth.parse(monthKey)
        val daysInMonth = yearMonth.lengthOfMonth()

        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        // Row 18 (0-indexed: 17): "Sales for Thomas Lee"
        val salesPersonName = extractSalesPersonName(
            sheet.getRow(17)?.getCell(0)?.stringCellValue ?: ""
        )

        val targets = mutableMapOf<String, Long>()
        val dailyEntries = mutableMapOf<String, MutableMap<String, Long>>()
        val skippedProducts = mutableListOf<String>()

        // Rows 20-31 (0-indexed: 19-30): product data rows
        for (rowIdx in 19..30) {
            val row = sheet.getRow(rowIdx) ?: continue
            val productName = row.getCell(0)?.stringCellValue?.trim() ?: continue

            val mapping = PRODUCT_TO_CATEGORY[productName]
            if (mapping == null) {
                skippedProducts.add(productName)
                continue
            }

            // Column B (index 1): Target
            val rawTarget = getCellNumericValue(row.getCell(1))
            val targetValue = if (mapping.isMoney) (rawTarget * 100).toLong() else rawTarget.toLong()
            targets[mapping.categoryId] = targetValue

            // Columns G-AK (index 6 to 6+30): Daily values for days 1-31
            for (day in 1..daysInMonth) {
                val cellIdx = 5 + day  // day 1 → col index 6 (G)
                val rawValue = getCellNumericValue(row.getCell(cellIdx))
                if (rawValue == 0.0) continue

                val value = if (mapping.isMoney) (rawValue * 100).toLong() else rawValue.toLong()
                val dateKey = String.format("%s-%02d", monthKey, day)

                dailyEntries
                    .getOrPut(dateKey) { mutableMapOf() }[mapping.categoryId] = value
            }
        }

        workbook.close()

        return XlsxImportResult(
            monthKey = monthKey,
            salesPersonName = salesPersonName,
            targets = targets,
            dailyEntries = dailyEntries,
            skippedProducts = skippedProducts
        )
    }

    /**
     * Extract month key (YYYY-MM) from filename.
     * Expected pattern: `SalesTracker_..._<Month>_<Year>.xlsx`
     */
    internal fun extractMonthKey(filename: String): String {
        // Strip extension and split by underscore
        val base = filename.removeSuffix(".xlsx").removeSuffix(".XLSX")
        val parts = base.split("_")

        // Find month + year from the last two underscore-separated tokens
        // e.g. [..., "March", "2026"]
        require(parts.size >= 2) { "Cannot extract month from filename: $filename" }

        val yearStr = parts.last()
        val monthStr = parts[parts.size - 2]

        val year = yearStr.toIntOrNull()
            ?: throw IllegalArgumentException("Cannot parse year from filename: $filename")
        val monthNum = MONTH_NAMES[monthStr.lowercase()]
            ?: throw IllegalArgumentException("Cannot parse month '$monthStr' from filename: $filename")

        return String.format("%04d-%02d", year, monthNum)
    }

    /**
     * Extract sales person name from "Sales for <Name>" cell.
     */
    private fun extractSalesPersonName(cellValue: String): String {
        val prefix = "Sales for "
        return if (cellValue.startsWith(prefix, ignoreCase = true)) {
            cellValue.removePrefix(prefix).trim()
        } else {
            cellValue.trim()
        }
    }

    /**
     * Safely read a numeric value from any cell type.
     */
    private fun getCellNumericValue(cell: org.apache.poi.ss.usermodel.Cell?): Double {
        if (cell == null) return 0.0
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.toDoubleOrNull() ?: 0.0
            org.apache.poi.ss.usermodel.CellType.FORMULA -> {
                try { cell.numericCellValue } catch (_: Exception) { 0.0 }
            }
            else -> 0.0
        }
    }
}
