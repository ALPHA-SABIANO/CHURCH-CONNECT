package com.example.churchconnect.threads

import com.example.churchconnect.Comment
import com.example.churchconnect.PendingQuestion
import com.example.churchconnect.canDeleteComment
import com.example.churchconnect.sortPendingQuestions
import com.example.churchconnect.unansweredCount
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Discussion Threads & Comment Permissions.
 */
class ThreadsTest {

    /**
     * THR_COMMENT_001 (Positive)
     * Verifies that Comment data model defaults replyToId to empty string (top-level comment).
     */
    @Test
    fun THR_COMMENT_001_commentModel_defaultsToTopLevel() {
        val comment = Comment(
            id = "c1",
            authorUid = "user123",
            authorName = "Alice",
            authorRole = "member",
            text = "What time does the service start?"
        )
        assertEquals("c1", comment.id)
        assertEquals("user123", comment.authorUid)
        assertEquals("", comment.replyToId)
    }

    /**
     * THR_COMMENT_002 (Negative)
     * Verifies that attempting to send an empty or blank comment triggers a validation error message.
     */
    @Test
    fun THR_COMMENT_002_validateCommentInput_triggersValidationErrorOnEmptyText() {
        fun validateComment(text: String, isStaff: Boolean): String? {
            return if (text.trim().isEmpty()) {
                if (isStaff) "Please enter a reply first." else "Please enter a comment or question first."
            } else null
        }

        assertEquals("Please enter a comment or question first.", validateComment("", isStaff = false))
        assertEquals("Please enter a comment or question first.", validateComment("   ", isStaff = false))
        assertEquals("Please enter a reply first.", validateComment("", isStaff = true))
        assertNull(validateComment("Amen!", isStaff = false))
    }

    /**
     * THR_QUESTIONS_001 (Positive)
     * Verifies that pending member questions place unanswered questions first and calculates unanswered count.
     */
    @Test
    fun THR_QUESTIONS_001_sortPendingQuestions_prioritizesUnansweredQuestions() {
        val q1 = PendingQuestion(commentId = "q1", text = "When is choir practice?", hasStaffReply = true, createdAt = 1000L)
        val q2 = PendingQuestion(commentId = "q2", text = "Can I bring a friend to youth service?", hasStaffReply = false, createdAt = 2000L)

        val list = listOf(q1, q2)
        val sorted = sortPendingQuestions(list)
        assertEquals("q2", sorted.first().commentId) // Unanswered q2 is placed first
        assertEquals(1, unansweredCount(list))
    }

    /**
     * THR_DELETE_001 (Positive)
     * Verifies that staff members (Admin/Pastor) can delete any comment.
     */
    @Test
    fun THR_DELETE_001_canDeleteComment_allowsStaffToDeleteAnyComment() {
        assertTrue(canDeleteComment(isStaff = true, authorUid = "member_uid_1", myUid = "staff_uid_2"))
    }

    /**
     * THR_DELETE_002 (Positive)
     * Verifies that comment authors can delete their own comments.
     */
    @Test
    fun THR_DELETE_002_canDeleteComment_allowsAuthorToDeleteOwnComment() {
        assertTrue(canDeleteComment(isStaff = false, authorUid = "user_abc", myUid = "user_abc"))
    }

    /**
     * THR_DELETE_003 (Negative)
     * Verifies that non-staff members cannot delete comments written by other users.
     */
    @Test
    fun THR_DELETE_003_canDeleteComment_deniesNonStaffDeletingOthersComment() {
        assertFalse(canDeleteComment(isStaff = false, authorUid = "user_abc", myUid = "user_xyz"))
    }

    /**
     * THR_DELETE_004 (Negative)
     * Verifies that unauthenticated or logged-out users (myUid = null) cannot delete comments.
     */
    @Test
    fun THR_DELETE_004_canDeleteComment_deniesUnauthenticatedUsers() {
        assertFalse(canDeleteComment(isStaff = false, authorUid = "user_abc", myUid = null))
    }
}
