package com.example.churchconnect

/** True for pastor and admin roles. */
fun isStaffRole(role: String): Boolean = role == "admin" || role == "pastor"

/** Words whose publication time has arrived, newest first. */
fun publishedWords(words: List<Word>, now: Long): List<Word> =
    words.filter { it.publishAt <= now }.sortedByDescending { it.publishAt }

/** Filter announcements by category, or return all if "All" is selected. */
fun filterByCategory(list: List<Announcement>, category: String): List<Announcement> =
    list.filter { category == "All" || it.category == category }

/** Total services attended per member UID. */
fun adultAttendanceCounts(records: List<AttendanceRecord>): Map<String, Int> =
    records.flatMap { it.presentUids }.groupingBy { it }.eachCount()

/** Determines if a user can delete a comment (must be staff or the comment author). */
fun canDeleteComment(isStaff: Boolean, authorUid: String, myUid: String?): Boolean =
    isStaff || (myUid != null && authorUid == myUid)

/** Validates if a date string is a valid yyyy-MM-dd date. */
fun isValidDate(date: String): Boolean = parseDateTime(date, "00:00") != null

/** Filters members by search query (matching name, phone, or email). */
fun filterMembers(members: List<Member>, query: String): List<Member> {
    if (query.isBlank()) return members
    val q = query.trim().lowercase()
    return members.filter {
        it.name.lowercase().contains(q) ||
                it.phone.lowercase().contains(q) ||
                it.email.lowercase().contains(q)
    }
}

/** Validates if phone number contains exactly 10 digits (e.g. Malawi format 0999123456 or 0888123456). */
fun isValidPhoneNumber(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length == 10
}

/** Validates basic email format (e.g. user@example.com). */
fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    val atIndex = trimmed.indexOf('@')
    val lastDotIndex = trimmed.lastIndexOf('.')
    return atIndex > 0 && lastDotIndex > atIndex + 1 && lastDotIndex < trimmed.length - 1
}

/** Formats attendance percentage and counts for attendance reports. */
fun formatAttendanceSummary(presentCount: Int, totalMembers: Int): String {
    if (totalMembers <= 0) return "0% (0 present)"
    val percentage = (presentCount * 100) / totalMembers
    return "$percentage% ($presentCount of $totalMembers present)"
}

/** Sorts member questions placing unanswered items first, then newest first. */
fun sortPendingQuestions(questions: List<PendingQuestion>): List<PendingQuestion> =
    questions.sortedWith(compareBy<PendingQuestion> { it.hasStaffReply }.thenByDescending { it.createdAt })

/** Counts unanswered member questions needing urgent staff response. */
fun unansweredCount(questions: List<PendingQuestion>): Int =
    questions.count { !it.hasStaffReply }
