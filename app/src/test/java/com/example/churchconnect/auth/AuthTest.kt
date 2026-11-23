package com.example.churchconnect.auth

import com.example.churchconnect.Member
import com.example.churchconnect.isStaffRole
import com.example.churchconnect.isValidEmail
import com.example.churchconnect.isValidPhoneNumber
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Authentication & User Role functionality.
 */
class AuthTest {

    /**
     * AUTH_REGISTER_001 (Positive)
     * Verifies that Member data class initializes with correct parameters and role.
     */
    @Test
    fun AUTH_REGISTER_001_memberModel_initializesCorrectly() {
        val member = Member(
            uid = "user_001",
            name = "John Phiri",
            email = "john@example.com",
            phone = "0999123456",
            role = "member"
        )
        assertEquals("user_001", member.uid)
        assertEquals("John Phiri", member.name)
        assertEquals("john@example.com", member.email)
        assertEquals("0999123456", member.phone)
        assertEquals("member", member.role)
    }

    /**
     * AUTH_REGISTER_002 (Negative)
     * Verifies that invalid phone numbers (less than 10 digits or contains letters) fail validation.
     */
    @Test
    fun AUTH_REGISTER_002_isValidPhoneNumber_rejectsInvalidPhoneNumbers() {
        assertFalse(isValidPhoneNumber("12345"))
        assertFalse(isValidPhoneNumber("0999ABCDEF"))
        assertFalse(isValidPhoneNumber(""))
    }

    /**
     * AUTH_LOGIN_001 (Positive)
     * Verifies that valid 10-digit phone numbers and properly formatted emails pass validation.
     */
    @Test
    fun AUTH_LOGIN_001_isValidIdentifier_acceptsValidEmailAndPhone() {
        assertTrue(isValidPhoneNumber("0999123456"))
        assertTrue(isValidPhoneNumber("0888123456"))
        assertTrue(isValidEmail("pastor@churchconnect.com"))
        assertTrue(isValidEmail("admin.user@domain.org"))
    }

    /**
     * AUTH_LOGIN_002 (Negative)
     * Verifies that malformed email addresses fail validation.
     */
    @Test
    fun AUTH_LOGIN_002_isValidEmail_rejectsMalformedEmails() {
        assertFalse(isValidEmail("plainaddress"))
        assertFalse(isValidEmail("@missingusername.com"))
        assertFalse(isValidEmail("username@.com"))
        assertFalse(isValidEmail("username@domain."))
    }

    /**
     * AUTH_ROLE_001 (Positive)
     * Verifies that Admin and Pastor roles are recognized as staff roles.
     */
    @Test
    fun AUTH_ROLE_001_isStaffRole_identifiesStaffMembers() {
        assertTrue(isStaffRole("admin"))
        assertTrue(isStaffRole("pastor"))
    }

    /**
     * AUTH_ROLE_002 (Negative)
     * Verifies that regular members and unrecognized role strings are not identified as staff.
     */
    @Test
    fun AUTH_ROLE_002_isStaffRole_rejectsNonStaffRoles() {
        assertFalse(isStaffRole("member"))
        assertFalse(isStaffRole("guest"))
        assertFalse(isStaffRole(""))
    }
}
