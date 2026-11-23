package com.example.churchconnect

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

object ChurchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // The app serves ONE church, stored in this single document.
    private val churchDoc = db.collection("church").document("main")

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Last loading error from Firebase (shown on screen so problems are visible). */
    var lastError by mutableStateOf<String?>(null)

    //ACCOUNTS
    fun currentUid(): String? = auth.currentUser?.uid

    /** Creates a user account with the specified role ("member", "pastor", or "admin"). */
    suspend fun register(name: String, phone: String, email: String, password: String, role: String = "member") {
        val cleanRole = role.lowercase().trim()
        val authEmail = if (email.isNotBlank() && isValidEmail(email)) {
            email.trim()
        } else if (phone.isNotBlank() && isValidPhoneNumber(phone)) {
            "${phone.filter { it.isDigit() }}@churchconnect.local"
        } else {
            throw Exception("Please provide a valid email address or 10-digit phone number.")
        }
        val result = auth.createUserWithEmailAndPassword(authEmail, password).await()
        val uid = result.user?.uid ?: throw Exception("Could not create the account.")
        online {
            db.collection("users").document(uid).set(
                mapOf(
                    "name" to name.trim(),
                    "phone" to phone.trim(),
                    "email" to (email.ifBlank { authEmail }),
                    "role" to cleanRole,
                    "createdAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    suspend fun login(identifier: String, password: String) {
        val trimmedId = identifier.trim()
        val resolvedEmail = if (isValidEmail(trimmedId)) {
            trimmedId
        } else if (isValidPhoneNumber(trimmedId)) {
            val querySnapshot = online {
                db.collection("users").whereEqualTo("phone", trimmedId).get().await()
            }
            val doc = querySnapshot.documents.firstOrNull()
                ?: throw Exception("No account found with phone number $trimmedId. Please register or use email.")
            doc.getString("email") ?: throw Exception("Email not found for this phone number.")
        } else {
            throw Exception("Please enter a valid 10-digit phone number or email address.")
        }

        auth.signInWithEmailAndPassword(resolvedEmail, password).await()
        ensureProfile()
    }

    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() = auth.signOut()

    suspend fun updateFcmToken(token: String) {
        val uid = currentUid() ?: return
        online {
            db.collection("users").document(uid).set(
                mapOf("fcmToken" to token),
                SetOptions.merge()
            ).await()
        }
    }

    /** Ensures user profile exists in Firestore without overwriting existing role. */
    private suspend fun ensureProfile() {
        val user = auth.currentUser ?: return
        try {
            online {
                val ref = db.collection("users").document(user.uid)
                val doc = ref.get().await()
                if (!doc.exists()) {
                    ref.set(
                        mapOf(
                            "name" to (user.displayName ?: user.email.orEmpty()),
                            "phone" to "",
                            "email" to user.email.orEmpty(),
                            "role" to "member",
                            "createdAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()
                }
            }
        } catch (e: Exception) {
            // Ignore if offline
        }
    }

    /** Real-time flow of the logged-in user's role ("admin", "pastor", or "member"). */
    fun userRoleFlow(uid: String?): Flow<String> = callbackFlow {
        if (uid.isNullOrBlank()) {
            trySend("member")
            awaitClose { }
            return@callbackFlow
        }
        val reg = db.collection("users").document(uid).addSnapshotListener { snap, e ->
            if (e != null) {
                lastError = e.localizedMessage
                trySend("member")
                return@addSnapshotListener
            }
            val r = snap?.getString("role")?.lowercase()?.trim() ?: "member"
            trySend(r)
        }
        awaitClose { reg.remove() }
    }

    /** "admin", "pastor" or "member". */
    suspend fun myRole(): String = try {
        online {
            val uid = currentUid()
            if (uid == null) "member"
            else {
                val doc = db.collection("users").document(uid).get().await()
                if (!doc.exists()) {
                    ensureProfile()
                    "member"
                } else {
                    doc.getString("role")?.lowercase()?.trim() ?: "member"
                }
            }
        }
    } catch (e: Exception) {
        "member"
    }

    /** Returns (name, role) of the logged-in user. */
    private suspend fun myProfile(): Pair<String, String> = try {
        online {
            val user = auth.currentUser ?: throw Exception("Please log in again.")
            val doc = db.collection("users").document(user.uid).get().await()
            if (!doc.exists()) {
                ensureProfile()
                (user.displayName?.ifBlank { null } ?: user.email.orEmpty()) to "member"
            } else {
                val name = doc.getString("name")?.ifBlank { null } ?: user.email.orEmpty()
                val role = doc.getString("role")?.lowercase()?.trim() ?: "member"
                name to role
            }
        }
    } catch (e: Exception) {
        val email = auth.currentUser?.email.orEmpty()
        email to "member"
    }

    /** Admin only (enforced by the database rules). */
    suspend fun setRole(uid: String, role: String) {
        online { db.collection("users").document(uid).update("role", role).await() }
    }

    // ---------- MEMBERS & ATTENDANCE (pastor/admin) ----------
    /** Registers a member manually (email is optional). */
    suspend fun addMember(name: String, phone: String, email: String = "") {
        online {
            val ref = db.collection("users").document()
            ref.set(
                mapOf(
                    "name" to name.trim(),
                    "phone" to phone.trim(),
                    "email" to email.trim(),
                    "role" to "member",
                    "createdAt" to System.currentTimeMillis(),
                    "registeredManually" to true
                )
            ).await()
        }
    }

    suspend fun deleteMember(uid: String) {
        online { db.collection("users").document(uid).delete().await() }
    }

    fun members(): Flow<List<Member>> =
        listen(db.collection("users")) {
            it.toObject(Member::class.java)?.copy(uid = it.id)
        }.map { list -> list.sortedBy { m -> m.name.lowercase() } }

    fun attendance(): Flow<List<AttendanceRecord>> =
        listen(db.collection("attendance")) {
            it.toObject(AttendanceRecord::class.java)?.copy(id = it.id)
        }

    suspend fun saveAttendance(date: String, presentUids: List<String>) {
        online {
            db.collection("attendance").document(date)
                .set(mapOf("date" to date, "presentUids" to presentUids)).await()
        }
    }

    suspend fun deleteAttendance(idOrDate: String) {
        online {
            db.collection("attendance").document(idOrDate).delete().await()
        }
    }

    // CHURCH PROFILE
    fun church(): Flow<Church?> = callbackFlow {
        val reg = churchDoc.addSnapshotListener { s, e ->
            if (e != null) {
                lastError = e.localizedMessage
                trySend(null)
                return@addSnapshotListener
            }
            trySend(s?.toObject(Church::class.java))
        }
        awaitClose { reg.remove() }
    }

    suspend fun saveChurch(name: String, location: String, serviceTimes: String, description: String) {
        online {
            churchDoc.set(
                mapOf(
                    "name" to name, "location" to location,
                    "serviceTimes" to serviceTimes, "description" to description
                ),
                SetOptions.merge()
            ).await()
        }
    }

    //INFORMATION, ANNOUNCEMENTS, EVENTS, WORDS
    fun infoPosts(): Flow<List<InfoPost>> =
        listen(sub("information").orderBy("createdAt", Query.Direction.DESCENDING)) {
            it.toObject(InfoPost::class.java)?.copy(id = it.id)
        }

    fun announcements(): Flow<List<Announcement>> =
        listen(sub("announcements").orderBy("createdAt", Query.Direction.DESCENDING)) {
            it.toObject(Announcement::class.java)?.copy(id = it.id)
        }

    fun events(): Flow<List<Event>> =
        listen(sub("events").orderBy("createdAt", Query.Direction.DESCENDING)) {
            it.toObject(Event::class.java)?.copy(id = it.id)
        }

    fun words(): Flow<List<Word>> =
        listen(sub("words").orderBy("publishAt", Query.Direction.DESCENDING)) {
            it.toObject(Word::class.java)?.copy(id = it.id)
        }

    suspend fun saveWord(id: String?, title: String, scripture: String, message: String, publishAt: Long) {
        val author = myProfile().first
        saveItem(
            "words", id,
            mapOf(
                "title" to title, "scripture" to scripture, "message" to message,
                "publishAt" to publishAt, "authorName" to author
            )
        )
        appContext?.let { ctx ->
            MyFirebaseMessagingService.showNotification(
                ctx,
                "New Word of Life: $title",
                scripture.ifBlank { message }
            )
        }
    }

    suspend fun saveItem(collection: String, id: String?, data: Map<String, Any>) {
        online {
            val col = sub(collection)
            val ref = if (id == null) col.document() else col.document(id)
            val author = myProfile().first
            val baseData = if (data.containsKey("authorName")) data else data + mapOf("authorName" to author)
            val payload = if (id == null) baseData + ("createdAt" to System.currentTimeMillis()) else baseData
            ref.set(payload, SetOptions.merge()).await()

            if (id == null && appContext != null) {
                val title = data["title"] as? String ?: "Church Update"
                val msg = when (collection) {
                    "announcements" -> data["message"] as? String ?: "New announcement shared"
                    "events" -> "${data["date"] ?: ""} - ${data["description"] as? String ?: "New event shared"}"
                    else -> "New update from church"
                }
                val notifTitle = when (collection) {
                    "announcements" -> "New Announcement: $title"
                    "events" -> "New Event: $title"
                    else -> title
                }
                MyFirebaseMessagingService.showNotification(appContext!!, notifTitle, msg)
            }
        }
    }

    suspend fun deleteItem(collection: String, id: String) {
        online { sub(collection).document(id).delete().await() }
    }

    //DETAILS + COMMENTS (announcements and events)
    fun detail(collection: String, id: String): Flow<DetailItem?> = callbackFlow {
        val reg = sub(collection).document(id).addSnapshotListener { s, e ->
            if (e != null) {
                lastError = e.localizedMessage
                trySend(null)
                return@addSnapshotListener
            }
            if (s == null || !s.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            val title = s.getString("title").orEmpty()
            val body: String
            val tag: String
            if (collection == "events") {
                body = "${s.getString("date").orEmpty()}\n${s.getString("description").orEmpty()}"
                tag = "Event"
            } else {
                body = s.getString("message").orEmpty()
                tag = s.getString("category").orEmpty().ifBlank { "Service" }
            }
            trySend(DetailItem(title, body, tag))
        }
        awaitClose { reg.remove() }
    }

    fun comments(collection: String, itemId: String): Flow<List<Comment>> =
        listen(sub(collection).document(itemId).collection("comments").orderBy("createdAt")) {
            it.toObject(Comment::class.java)?.copy(id = it.id)
        }

    suspend fun addComment(collection: String, itemId: String, text: String, replyToId: String = "") {
        val (name, role) = myProfile()
        online {
            sub(collection).document(itemId).collection("comments").document()
                .set(
                    mapOf(
                        "authorUid" to (currentUid() ?: ""),
                        "authorName" to name,
                        "authorRole" to role,
                        "text" to text,
                        "replyToId" to replyToId,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
        }
    }

    suspend fun deleteComment(collection: String, itemId: String, commentId: String) {
        online {
            sub(collection).document(itemId).collection("comments").document(commentId).delete().await()
        }
    }

    /** Flow of in-app notifications for users upon login. */
    fun notifications(): Flow<List<NotificationItem>> = callbackFlow {
        val reg = sub("announcements").orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snap, _ ->
            if (snap != null) {
                val list = snap.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: "Church Announcement"
                    val msg = doc.getString("message") ?: "Tap to view announcement details"
                    val time = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    NotificationItem(
                        id = doc.id,
                        title = "Announcement: $title",
                        message = msg,
                        type = "Announcement",
                        targetCollection = "announcements",
                        targetItemId = doc.id,
                        createdAt = time
                    )
                }
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { reg.remove() }
    }

    /** Flow of all member questions across announcements and events for Pastor / Admin priority response. */
    fun pendingQuestions(): Flow<List<PendingQuestion>> = callbackFlow {
        val listenerRegs = mutableListOf<ListenerRegistration>()

        fun refresh() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resultList = mutableListOf<PendingQuestion>()
                    val collections = listOf("announcements", "events")
                    for (colName in collections) {
                        val itemsSnap = sub(colName).get().await()
                        for (itemDoc in itemsSnap.documents) {
                            val itemTitle = itemDoc.getString("title") ?: "Church Update"
                            val itemId = itemDoc.id
                            val commentsSnap = itemDoc.reference.collection("comments").get().await()
                            val comments = commentsSnap.documents.mapNotNull { it.toObject(Comment::class.java)?.copy(id = it.id) }

                            val topLevelComments = comments.filter { it.replyToId.isBlank() && it.authorRole == "member" }
                            val staffRepliesMap = comments.filter { it.replyToId.isNotBlank() && (it.authorRole == "pastor" || it.authorRole == "admin") }
                                .groupBy { it.replyToId }

                            for (c in topLevelComments) {
                                val hasReply = staffRepliesMap.containsKey(c.id)
                                resultList.add(
                                    PendingQuestion(
                                        commentId = c.id,
                                        authorName = c.authorName.ifBlank { "Member" },
                                        text = c.text,
                                        collection = colName,
                                        itemId = itemId,
                                        itemTitle = itemTitle,
                                        createdAt = c.createdAt,
                                        hasStaffReply = hasReply
                                    )
                                )
                            }
                        }
                    }
                    trySend(sortPendingQuestions(resultList))
                } catch (e: Exception) {
                    trySend(emptyList())
                }
            }
        }

        refresh()
        val reg1 = sub("announcements").addSnapshotListener { _, _ -> refresh() }
        val reg2 = sub("events").addSnapshotListener { _, _ -> refresh() }
        listenerRegs.add(reg1)
        listenerRegs.add(reg2)

        awaitClose { listenerRegs.forEach { it.remove() } }
    }

    //helpers
    private fun sub(name: String) = churchDoc.collection(name)

    private suspend fun <T> online(block: suspend () -> T): T =
        try {
            withTimeout(15_000) { block() }
        } catch (e: TimeoutCancellationException) {
            throw Exception(
                "Could not reach the server. Check your internet and make sure the Firestore database has been created."
            )
        }

    private fun <T> listen(query: Query, map: (DocumentSnapshot) -> T?): Flow<List<T>> = callbackFlow {
        val reg = query.addSnapshotListener { snap, e ->
            if (e != null) {
                lastError = e.localizedMessage ?: "Could not load data."
                trySend(emptyList<T>())
                return@addSnapshotListener
            }
            lastError = null
            if (snap != null) trySend(snap.documents.mapNotNull(map))
        }
        awaitClose { reg.remove() }
    }
}