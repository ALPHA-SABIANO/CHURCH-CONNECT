package com.example.churchconnect.members

import com.example.churchconnect.Member
import com.example.churchconnect.filterMembers
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Member Directory & Search filtering.
 */
class MembersTest {

    private val sampleMembers = listOf(
        Member(uid = "m1", name = "Grace Banda", email = "grace@example.com", phone = "0999111222"),
        Member(uid = "m2", name = "Peter Phiri", email = "peter@church.org", phone = "0888333444"),
        Member(uid = "m3", name = "Samuel Tembo", email = "samuel@example.com", phone = "0999555666")
    )

    /**
     * MEM_SEARCH_001 (Positive)
     * Verifies that filterMembers matches members by name, email, or phone number (case-insensitive).
     */
    @Test
    fun MEM_SEARCH_001_filterMembers_matchesByNameEmailOrPhone() {
        val byName = filterMembers(sampleMembers, "Grace")
        assertEquals(1, byName.size)
        assertEquals("m1", byName.first().uid)

        val byPhone = filterMembers(sampleMembers, "0888333444")
        assertEquals(1, byPhone.size)
        assertEquals("m2", byPhone.first().uid)

        val byEmailDomain = filterMembers(sampleMembers, "example.com")
        assertEquals(2, byEmailDomain.size)
    }

    /**
     * MEM_SEARCH_002 (Negative)
     * Verifies that searching with a query that matches no member returns an empty list.
     */
    @Test
    fun MEM_SEARCH_002_filterMembers_returnsEmptyWhenNoMatchFound() {
        val result = filterMembers(sampleMembers, "NonExistentName")
        assertTrue(result.isEmpty())
    }

    /**
     * MEM_SEARCH_003 (Positive)
     * Verifies that a blank search query returns the complete member list unmodified.
     */
    @Test
    fun MEM_SEARCH_003_filterMembers_returnsAllMembersOnBlankQuery() {
        val result = filterMembers(sampleMembers, "   ")
        assertEquals(3, result.size)
    }
}
