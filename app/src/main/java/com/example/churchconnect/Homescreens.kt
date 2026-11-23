@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.churchconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// HOME (members, pastor, admin)

@Composable
fun HomeScreen(
    onManage: () -> Unit,
    onOpenDetail: (String, String) -> Unit,
    onNotificationClick: () -> Unit,
    onLogout: () -> Unit
) {
    val church by remember { ChurchRepository.church() }.collectAsState(initial = null)
    val infoPosts by remember { ChurchRepository.infoPosts() }.collectAsState(initial = emptyList())
    val announcements by remember { ChurchRepository.announcements() }.collectAsState(initial = emptyList())
    val events by remember { ChurchRepository.events() }.collectAsState(initial = emptyList())
    val words by remember { ChurchRepository.words() }.collectAsState(initial = emptyList())
    val role = rememberRole()
    val isStaff = isStaffRole(role)
    val now = rememberNow()
    var tab by remember { mutableIntStateOf(0) }
    var newsFilter by remember { mutableStateOf("All") }

    val scope = rememberCoroutineScope()
    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var toDeleteNotice by remember { mutableStateOf<Announcement?>(null) }

    val roleLabel = when (role) {
        "admin" -> "Admin Account"
        "pastor" -> "Pastor Account"
        else -> "Ordinary Member"
    }
    val subtitleText = buildString {
        if (!church?.location.isNullOrBlank()) append("${church?.location} • ")
        append(roleLabel)
    }

    Box(Modifier.fillMaxSize().background(ScreenBg)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = church?.name?.ifBlank { "Church Connect" } ?: "Church Connect",
                subtitle = subtitleText,
                onNotificationClick = onNotificationClick,
                action = if (isStaff) {
                    {
                        IconButton(onClick = onManage) {
                            Icon(Icons.Default.Settings, "Manage church", tint = ChurchGold)
                        }
                    }
                } else null
            )

            ChurchRepository.lastError?.let {
                Text("Could not load data: $it", color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> {
                        val c = church
                        val latest = publishedWords(words, now).firstOrNull()

                        item { Text("Church Connect • Stay Informed & Connected", fontWeight = FontWeight.Bold, color = TextDark) }

                        if (c == null) {
                            item { Text("The church profile has not been set up yet.", color = TextGray) }
                        } else {
                            item { InfoCard("About Our Church", c.description.ifBlank { "No description yet." }, ChurchBlue) }
                            item { InfoCard("Location", c.location.ifBlank { "Not provided." }, ChurchGold) }
                            item { InfoCard("Service Times", c.serviceTimes.ifBlank { "Not provided." }, ChurchGreen) }
                        }

                        if (latest != null) {
                            item { WordCard(latest, subtitle = "Word of Today") }
                        }

                        if (infoPosts.isNotEmpty()) {
                            item { Text("Information & Notices", fontWeight = FontWeight.Bold, color = TextDark) }
                        }
                        items(infoPosts, key = { it.id }) { InfoCard(it.title, it.details, ChurchBlue, authorName = it.authorName, createdAt = it.createdAt) }
                    }
                    1 -> {
                        item { FilterRow(listOf("All") + ANNOUNCEMENT_CATEGORIES, newsFilter) { newsFilter = it } }
                        val shown = filterByCategory(announcements, newsFilter)
                        if (shown.isEmpty()) {
                            item { Text("No announcements or notices here yet.", color = TextGray) }
                        }
                        items(shown, key = { it.id }) { a ->
                            InfoCard(
                                a.title, "${a.message}\n\nTap to ${if (isStaff) "view and reply" else "comment or ask a question"}", ChurchGold,
                                tag = a.category,
                                authorName = a.authorName,
                                createdAt = a.createdAt,
                                onClick = { onOpenDetail("announcements", a.id) },
                                onDelete = if (isStaff) ({ toDeleteNotice = a }) else null
                            )
                        }
                    }
                    2 -> {
                        if (events.isEmpty()) {
                            item { Text("No events yet.", color = TextGray) }
                        }
                        items(events, key = { it.id }) { e ->
                            InfoCard(
                                e.title, "${e.date}\n${e.description}\n\nTap to ${if (isStaff) "view and reply" else "comment or ask a question"}", ChurchGreen,
                                tag = "Event",
                                authorName = e.authorName,
                                createdAt = e.createdAt,
                                onClick = { onOpenDetail("events", e.id) }
                            )
                        }
                    }
                    else -> {
                        val published = publishedWords(words, now)
                        if (published.isEmpty()) {
                            item { Text("No words of encouragement yet. Check back soon.", color = TextGray) }
                        }
                        items(published, key = { it.id }) { WordCard(it) }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Exit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Exit", fontWeight = FontWeight.Bold)
                    }
                }
            }

            NavigationBar(containerColor = Color.White) {
                val tabs = listOf(
                    "Home" to Icons.Default.Home,
                    "News" to Icons.Default.Campaign,
                    "Events" to Icons.Default.Event,
                    "Word" to Icons.Default.AutoStories
                )
                tabs.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icon, label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ChurchBlue,
                            indicatorColor = ChurchBlue,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )
                }
            }
        }

        if (tab == 1 && isStaff) {
            FloatingActionButton(
                onClick = { showAddNoticeDialog = true },
                containerColor = ChurchGold,
                contentColor = ChurchBlueDark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 70.dp, end = 16.dp)
                    .navigationBarsPadding()
            ) { Icon(Icons.Default.Add, "Add Notice") }
        }
    }

    if (showAddNoticeDialog) {
        FormDialog(
            title = "New Notice / Announcement",
            labels = listOf("Title", "Message"),
            initial = listOf("", ""),
            onDismiss = { showAddNoticeDialog = false },
            choiceTitle = "Category / Tag",
            choices = ANNOUNCEMENT_CATEGORIES,
            initialChoice = if (newsFilter in ANNOUNCEMENT_CATEGORIES) newsFilter else "Notice"
        ) { v ->
            scope.launch {
                try {
                    ChurchRepository.saveItem("announcements", null, mapOf("title" to v[0], "message" to v[1], "category" to v[2]))
                } catch (e: Exception) {
                    // ignore
                }
            }
            showAddNoticeDialog = false
        }
    }

    toDeleteNotice?.let { a ->
        AlertDialog(
            onDismissRequest = { toDeleteNotice = null },
            containerColor = Color.White,
            title = { Text("Delete Notice?", color = ChurchBlue, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${a.title}'? This cannot be undone.", color = TextDark) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ChurchRepository.deleteItem("announcements", a.id)
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                        toDeleteNotice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { toDeleteNotice = null }) { Text("Cancel", color = TextGray) } }
        )
    }
}

//DETAILS + COMMENTS

@Composable
fun DetailScreen(collection: String, itemId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val detail by remember(collection, itemId) { ChurchRepository.detail(collection, itemId) }
        .collectAsState(initial = null)
    val comments by remember(collection, itemId) { ChurchRepository.comments(collection, itemId) }
        .collectAsState(initial = emptyList())
    val role = rememberRole()
    val isStaff = isStaffRole(role)
    val myUid = ChurchRepository.currentUid()
    var text by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    val topLevelComments = comments.filter { it.replyToId.isBlank() }
    val repliesMap = comments.filter { it.replyToId.isNotBlank() }.groupBy { it.replyToId }

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader(
            if (collection == "events") "Event" else "Announcement",
            if (isStaff) "Replies and discussion" else "Comments and questions", onBack = onBack
        )

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoCard(
                    detail?.title ?: "Loading...", detail?.body ?: "",
                    if (collection == "events") ChurchGreen else ChurchGold,
                    tag = detail?.tag
                )
            }
            item { Text(if (isStaff) "Replies (${comments.size})" else "Comments (${comments.size})", fontWeight = FontWeight.Bold, color = TextDark) }
            if (comments.isEmpty()) {
                item { Text(if (isStaff) "No replies yet." else "No comments yet. Be the first to ask a question.", color = TextGray) }
            }
            items(topLevelComments, key = { it.id }) { c ->
                CommentCard(
                    c,
                    canDelete = canDeleteComment(isStaff, c.authorUid, myUid),
                    onDelete = {
                        scope.launch {
                            try {
                                ChurchRepository.deleteComment(collection, itemId, c.id)
                            } catch (e: Exception) {
                                message = e.localizedMessage
                            }
                        }
                    },
                    onReply = { replyingTo = c }
                )
                repliesMap[c.id]?.forEach { reply ->
                    Box(Modifier.padding(start = 24.dp)) {
                        CommentCard(
                            reply,
                            canDelete = canDeleteComment(isStaff, reply.authorUid, myUid),
                            onDelete = {
                                scope.launch {
                                    try {
                                        ChurchRepository.deleteComment(collection, itemId, reply.id)
                                    } catch (e: Exception) {
                                        message = e.localizedMessage
                                    }
                                }
                            },
                            onReply = null
                        )
                    }
                }
            }
        }

        message?.let {
            Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        replyingTo?.let { target ->
            Row(
                Modifier.fillMaxWidth().background(SoftBlue).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Replying to ${target.authorName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ChurchBlue, modifier = Modifier.weight(1f))
                IconButton(onClick = { replyingTo = null }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Cancel reply", tint = ChurchBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().imePadding().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    if (it.isNotBlank() && (message == "Please enter a comment or question first." || message == "Please enter a reply first.")) {
                        message = null
                    }
                },
                placeholder = { Text(if (isStaff) "Write a reply..." else "Write a comment or question...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                enabled = !sending,
                onClick = {
                    val t = text.trim()
                    if (t.isEmpty()) {
                        message = if (isStaff) "Please enter a reply first." else "Please enter a comment or question first."
                    } else {
                        sending = true
                        scope.launch {
                            try {
                                ChurchRepository.addComment(collection, itemId, t, replyToId = replyingTo?.id ?: "")
                                text = ""
                                replyingTo = null
                                message = null
                            } catch (e: Exception) {
                                message = e.localizedMessage
                            }
                            sending = false
                        }
                    }
                }
            ) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = ChurchBlue) }
        }
    }
}

// NOTIFICATIONS CENTER

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenDetail: (String, String) -> Unit
) {
    val notifications by remember { ChurchRepository.notifications() }.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader(
            title = "Notification Center",
            subtitle = "Recent church announcements and updates",
            onBack = onBack
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.NotificationsNone, null, tint = TextGray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No notifications yet", fontWeight = FontWeight.Bold, color = TextDark)
                            Text("You will see real-time church updates and replies here.", fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            }
            items(notifications, key = { it.id }) { item ->
                InfoCard(
                    title = item.title,
                    body = "${item.message}\n\nTap to view details",
                    accent = ChurchBlue,
                    tag = item.type,
                    createdAt = item.createdAt,
                    onClick = {
                        if (item.targetCollection.isNotBlank() && item.targetItemId.isNotBlank()) {
                            onOpenDetail(item.targetCollection, item.targetItemId)
                        }
                    }
                )
            }
        }
    }
}