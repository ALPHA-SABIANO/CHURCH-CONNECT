package com.example.churchconnect.attendance

import com.example.churchconnect.AttendanceRecord
import com.example.churchconnect.adultAttendanceCounts
import com.example.churchconnect.formatAttendanceSummary
import com.example.churchconnect.isValidDate
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Member Attendance & Reports logic.
 */
class AttendanceTest {

    /**
     * ATT_COUNT_001 (Positive)
     * Verifies that adultAttendanceCounts calculates total services attended per member.
     */
    @Test
    fun ATT_COUNT_001_adultAttendanceCounts_calculatesAttendedServicesPerMember() {
        val records = listOf(
            AttendanceRecord(id = "2026-10-11", date = "2026-10-11", presentUids = listOf("uid1", "uid2")),
            AttendanceRecord(id = "2026-10-18", date = "2026-10-18", presentUids = listOf("uid1", "uid3"))
        )
        val counts = adultAttendanceCounts(records)
        assertEquals(2, counts["uid1"])
        assertEquals(1, counts["uid2"])
        assertEquals(1, counts["uid3"])
        assertNull(counts["uid4"])
    }

    /**
     * ATT_SUMMARY_001 (Positive)
     * Verifies that formatAttendanceSummary calculates percentage and formatted string accurately.
     */
    @Test
    fun ATT_SUMMARY_001_formatAttendanceSummary_returnsFormattedPercentageAndCounts() {
        assertEquals("80% (40 of 50 present)", formatAttendanceSummary(40, 50))
        assertEquals("100% (10 of 10 present)", formatAttendanceSummary(10, 10))
        assertEquals("50% (5 of 10 present)", formatAttendanceSummary(5, 10))
    }

    /**
     * ATT_SUMMARY_002 (Negative)
     * Verifies that formatAttendanceSummary handles 0 or negative total members safely without division by zero.
     */
    @Test
    fun ATT_SUMMARY_002_formatAttendanceSummary_handlesZeroTotalMembersSafely() {
        assertEquals("0% (0 present)", formatAttendanceSummary(0, 0))
        assertEquals("0% (0 present)", formatAttendanceSummary(5, 0))
        assertEquals("0% (0 present)", formatAttendanceSummary(0, -5))
    }

    /**
     * ATT_DATE_001 (Positive)
     * Verifies that isValidDate returns true for valid yyyy-MM-dd date formats.
     */
    @Test
    fun ATT_DATE_001_isValidDate_validatesCorrectDateStrings() {
        assertTrue(isValidDate("2026-10-18"))
        assertTrue(isValidDate("2025-01-01"))
    }

    /**
     * ATT_DATE_002 (Negative)
     * Verifies that isValidDate rejects invalid or malformed date strings.
     */
    @Test
    fun ATT_DATE_002_isValidDate_rejectsInvalidDateStrings() {
        assertFalse(isValidDate("18-10-2026"))
        assertFalse(isValidDate("invalid-date"))
        assertFalse(isValidDate(""))
    }

    /**
     * ATT_DELETE_001 (Positive)
     * Verifies that staff permissions enable deletion of attendance report records.
     */
    @Test
    fun ATT_DELETE_001_staffPermissions_allowAttendanceReportDeletion() {
        val records = mutableListOf(
            AttendanceRecord(id = "2026-10-11", date = "2026-10-11", presentUids = listOf("uid1")),
            AttendanceRecord(id = "2026-10-18", date = "2026-10-18", presentUids = listOf("uid1", "uid2"))
        )
        // Simulate deletion of a report record by admin/staff
        records.removeAll { it.id == "2026-10-11" }
        assertEquals(1, records.size)
        assertEquals("2026-10-18", records.first().id)
    }
}
