package com.example.churchconnect

data class Church(
    val name: String = "",
    val location: String = "",
    val serviceTimes: String = "",
    val description: String = ""
)

data class InfoPost(
    val id: String = "",
    val title: String = "",
    val details: String = "",
    val authorName: String = "",
    val createdAt: Long = 0
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "Service",
    val authorName: String = "",
    val createdAt: Long = 0
)

data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val authorName: String = "",
    val createdAt: Long = 0
)

data class Word(
    val id: String = "",
    val title: String = "",
    val scripture: String = "",
    val message: String = "",
    val publishAt: Long = 0,
    val authorName: String = "",
    val createdAt: Long = 0
)

data class Comment(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorRole: String = "member",
    val text: String = "",
    val replyToId: String = "",
    val createdAt: Long = 0
)

data class Member(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "member",
    val createdAt: Long = 0
)

data class AttendanceRecord(
    val id: String = "",
    val date: String = "",
    val presentUids: List<String> = emptyList()
)

/** Title, text and category tag of an announcement or event (details screen). */
data class DetailItem(val title: String, val body: String, val tag: String = "")

/** An in-app notification item displayed in the Notification Center. */
data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "Announcement", // "Announcement", "Event", "Word", "Reply"
    val targetCollection: String = "",
    val targetItemId: String = "",
    val createdAt: Long = 0
)

/** A question/comment asked by a member needing Pastor or Admin response. */
data class PendingQuestion(
    val commentId: String = "",
    val authorName: String = "",
    val text: String = "",
    val collection: String = "", // "announcements" or "events"
    val itemId: String = "",
    val itemTitle: String = "",
    val createdAt: Long = 0,
    val hasStaffReply: Boolean = false
)
