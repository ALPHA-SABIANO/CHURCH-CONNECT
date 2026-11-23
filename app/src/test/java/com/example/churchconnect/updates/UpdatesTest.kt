package com.example.churchconnect.updates

import com.example.churchconnect.ANNOUNCEMENT_CATEGORIES
import com.example.churchconnect.Announcement
import com.example.churchconnect.Word
import com.example.churchconnect.filterByCategory
import com.example.churchconnect.parseDateTime
import com.example.churchconnect.publishedWords
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for News, Announcements & Word of Life publishing.
 */
class UpdatesTest {

    /**
     * UPD_ANNOUNCEMENT_001 (Positive)
     * Verifies announcement categories list and filtering by category.
     */
    @Test
    fun UPD_ANNOUNCEMENT_001_filterByCategory_returnsMatchingCategoryItems() {
        assertEquals(listOf("Service", "Activity", "Notice"), ANNOUNCEMENT_CATEGORIES)

        val list = listOf(
            Announcement(id = "1", title = "Sunday Mass", category = "Service"),
            Announcement(id = "2", title = "Youth Conference", category = "Notice"),
            Announcement(id = "3", title = "Midweek Choir Practice", category = "Activity"),
            Announcement(id = "4", title = "Evening Prayer", category = "Service")
        )

        val serviceFilter = filterByCategory(list, "Service")
        assertEquals(2, serviceFilter.size)
        assertEquals(listOf("1", "4"), serviceFilter.map { it.id })

        val allFilter = filterByCategory(list, "All")
        assertEquals(4, allFilter.size)
    }

    /**
     * UPD_ANNOUNCEMENT_002 (Negative)
     * Verifies filtering with a non-existent category returns empty list.
     */
    @Test
    fun UPD_ANNOUNCEMENT_002_filterByCategory_returnsEmptyForNonExistentCategory() {
        val list = listOf(
            Announcement(id = "1", category = "Service"),
            Announcement(id = "2", category = "Event")
        )
        val result = filterByCategory(list, "General")
        assertTrue(result.isEmpty())
    }

    /**
     * UPD_WORD_001 (Positive)
     * Verifies that words scheduled in the past or present are returned in descending order.
     */
    @Test
    fun UPD_WORD_001_publishedWords_returnsPublishedWordsInOrder() {
        val words = listOf(
            Word(id = "w1", title = "Word 1", publishAt = 1000L),
            Word(id = "w2", title = "Word 2", publishAt = 3000L),
            Word(id = "w3", title = "Word 3", publishAt = 2000L)
        )
        val published = publishedWords(words, now = 2500L)
        assertEquals(2, published.size)
        assertEquals("w3", published[0].id) // 2000L is newer than 1000L
        assertEquals("w1", published[1].id)
    }

    /**
     * UPD_WORD_002 (Negative)
     * Verifies that future scheduled words (publishAt > now) are hidden from users.
     */
    @Test
    fun UPD_WORD_002_publishedWords_hidesFutureScheduledWords() {
        val words = listOf(
            Word(id = "past", publishAt = 1000L),
            Word(id = "future", publishAt = 5000L)
        )
        val result = publishedWords(words, now = 2000L)
        assertEquals(1, result.size)
        assertEquals("past", result.first().id)
    }

    /**
     * UPD_DATE_001 (Positive)
     * Verifies that parseDateTime correctly parses valid date (yyyy-MM-dd) and time (HH:mm) strings.
     */
    @Test
    fun UPD_DATE_001_parseDateTime_parsesValidStrings() {
        val timestamp = parseDateTime("2026-10-18", "06:30")
        assertNotNull(timestamp)
        assertTrue(timestamp!! > 0L)
    }

    /**
     * UPD_DATE_002 (Negative)
     * Verifies that parseDateTime returns null for invalid date/time formats.
     */
    @Test
    fun UPD_DATE_002_parseDateTime_returnsNullForInvalidFormats() {
        assertNull(parseDateTime("invalid-date", "06:30"))
        assertNull(parseDateTime("2026-10-18", "invalid-time"))
        assertNull(parseDateTime("", ""))
    }
}
