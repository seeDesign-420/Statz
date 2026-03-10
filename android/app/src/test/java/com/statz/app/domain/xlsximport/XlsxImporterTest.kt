package com.statz.app.domain.xlsximport

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.InputStream

/**
 * Unit tests for XlsxImporter parsing logic.
 * Uses the real sample XLSX from test resources.
 */
class XlsxImporterTest {

    private lateinit var importer: XlsxImporter

    @Before
    fun setUp() {
        importer = XlsxImporter()
    }

    // ── Month Key Extraction ────────────────────────────────────

    @Test
    fun `extractMonthKey parses March 2026`() {
        val key = importer.extractMonthKey(
            "SalesTracker_SABC1758_-_MTN_Store_-_Southcoast_Mall_68_-_Shelly_Beach_March_2026.xlsx"
        )
        assertEquals("2026-03", key)
    }

    @Test
    fun `extractMonthKey parses January 2025`() {
        val key = importer.extractMonthKey("SalesTracker_Store_January_2025.xlsx")
        assertEquals("2025-01", key)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `extractMonthKey throws on invalid filename`() {
        importer.extractMonthKey("random_file.xlsx")
    }

    // ── Full Parse ──────────────────────────────────────────────

    @Test
    fun `parse extracts correct month key`() {
        val result = parseTestFile()
        assertEquals("2026-03", result.monthKey)
    }

    @Test
    fun `parse extracts sales person name`() {
        val result = parseTestFile()
        assertEquals("Thomas Lee", result.salesPersonName)
    }

    @Test
    fun `parse extracts correct targets`() {
        val result = parseTestFile()
        assertEquals(26L, result.targets["new"])
        assertEquals(17L, result.targets["upgrade"])
        assertEquals(1200000L, result.targets["accessories"]) // R12000 → 1200000 cents
        assertEquals(5L, result.targets["insurance"])
        assertEquals(4L, result.targets["fiber"])
        assertEquals(0L, result.targets["home_wifi_contract"]) // FWA→home_wifi_contract, target is 0 in XLSX
        assertEquals(6L, result.targets["sme_new"])
        assertEquals(6L, result.targets["sme_up"])
    }

    @Test
    fun `parse FWA maps to home_wifi_contract with target 0`() {
        val result = parseTestFile()
        // FWA row (row 31) has target = 0
        assertEquals(0L, result.targets["home_wifi_contract"])
    }

    @Test
    fun `parse skips Combined Accessory QTY PP Sim Kits and MBB`() {
        val result = parseTestFile()
        assertTrue("Combined" in result.skippedProducts)
        assertTrue("Accessory QTY" in result.skippedProducts)
        assertTrue("PP Sim Kits" in result.skippedProducts)
        assertTrue("MBB" in result.skippedProducts)
        assertEquals(4, result.skippedProducts.size)
    }

    @Test
    fun `parse extracts daily values for day 5`() {
        val result = parseTestFile()
        val day5 = result.dailyEntries["2026-03-05"]
        assertNotNull(day5)
        // Row 20 (New lines): day 5 = 1
        assertEquals(1L, day5!!["new"])
        // Row 21 (Upgrades): day 5 = 2
        assertEquals(2L, day5["upgrade"])
    }

    @Test
    fun `parse extracts daily values for day 7 SME new`() {
        val result = parseTestFile()
        val day7 = result.dailyEntries["2026-03-07"]
        assertNotNull(day7)
        // Row 29 (SME new): day 7 (col M, index 12) = 1
        assertEquals(1L, day7!!["sme_new"])
    }

    @Test
    fun `parse extracts daily values for day 8 FWA`() {
        val result = parseTestFile()
        val day8 = result.dailyEntries["2026-03-08"]
        assertNotNull(day8)
        // Row 31 (FWA): day 8 (col N, index 13) = 1
        assertEquals(1L, day8!!["home_wifi_contract"])
    }

    @Test
    fun `parse creates no daily entries for days with all zeros`() {
        val result = parseTestFile()
        // Day 9 onwards should have no data (all zeros)
        assertNull(result.dailyEntries["2026-03-09"])
        assertNull(result.dailyEntries["2026-03-15"])
        assertNull(result.dailyEntries["2026-03-31"])
    }

    @Test
    fun `accessory R Value converts rands to cents`() {
        val result = parseTestFile()
        // Accessory R Value target is R12000 = 1200000 cents
        assertEquals(1200000L, result.targets["accessories"])
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun parseTestFile(): XlsxImportResult {
        val filename = "SalesTracker_SABC1758_-_MTN_Store_-_Southcoast_Mall_68_-_Shelly_Beach_March_2026.xlsx"
        val stream: InputStream = javaClass.classLoader!!.getResourceAsStream(filename)
            ?: throw IllegalStateException("Test resource not found: $filename")
        return importer.parse(stream, filename)
    }
}
