@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.churchconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import com.example.churchconnect.workers.ChurchWorkManager
import kotlinx.coroutines.launch

// MANAGE HOME (pastor / admin)

@Composable
fun ManageHomeScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit
) {
    val members by remember { ChurchRepository.members() }.collectAsState(initial = emptyList())
    val infoPosts by remember { ChurchRepository.infoPosts() }.collectAsState(initial = emptyList())
    val announcements by remember { ChurchRepository.announcements() }.collectAsState(initial = emptyList())
    val events by remember { ChurchRepository.events() }.collectAsState(initial = emptyList())
    val words by remember { ChurchRepository.words() }.collectAsState(initial = emptyList())
    val role = rememberRole()
    val isPastor = role == "pastor"
    val isAdmin = role == "admin"
    val now = rememberNow()
    val scheduled = words.count { it.publishAt > now }
    val questions by remember { ChurchRepository.pendingQuestions() }.collectAsState(initial = emptyList())
    val unanswered = unansweredCount(questions)

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader(
            title = "Manage Church",
            subtitle = if (isPastor) "Pastor Management Tools" else "Admin Tools",
            onBack = onBack
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptionCard(
                Icons.Default.QuestionAnswer, "Member Questions & Inquiries",
                if (unanswered > 0) "$unanswered question(s) awaiting response!" else "View member questions across announcements & events",
                if (unanswered > 0) Color(0xFFD32F2F) else ChurchBlue
            ) { onOpen("pending_questions") }

            OptionCard(
                Icons.Default.Campaign, "Announcements",
                "${announcements.size} announcement(s) saved", ChurchGold
            ) { onOpen("manage/ann") }

            OptionCard(
                Icons.Default.Event, "Events",
                "${events.size} event(s) saved", ChurchGreen
            ) { onOpen("manage/events") }

            if (isPastor) {
                OptionCard(
                    Icons.Default.AutoStories, "Word of Faith",
                    "${words.size} saved, $scheduled scheduled",
                    ChurchBlue
                ) { onOpen("manage/words") }
            }

            OptionCard(
                Icons.Default.Home, "Church Information",
                "Profile and ${infoPosts.size} information post(s)", ChurchBlue
            ) { onOpen("manage/info") }

            OptionCard(
                Icons.Default.People, "Members",
                "${members.size} registered" + if (isAdmin) " (Admin role management)" else "", ChurchGreen
            ) { onOpen("members") }

            OptionCard(
                Icons.Default.CheckCircle, "Attendance & Reports",
                "Mark member attendance and view reports", ChurchGold
            ) { onOpen("attendance") }

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
}

// MANAGE: INFORMATION / ANNOUNCEMENTS / EVENTS / WORDS

@Composable
fun ManageListScreen(section: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val role = rememberRole()
    val isAdmin = role == "admin"
    val isPastor = role == "pastor"
    val now = rememberNow()
    val church by remember { ChurchRepository.church() }.collectAsState(initial = null)
    val infoPosts by remember { ChurchRepository.infoPosts() }.collectAsState(initial = emptyList())
    val announcements by remember { ChurchRepository.announcements() }.collectAsState(initial = emptyList())
    val events by remember { ChurchRepository.events() }.collectAsState(initial = emptyList())
    val words by remember { ChurchRepository.words() }.collectAsState(initial = emptyList())

    var dialog by remember { mutableStateOf<String?>(null) }          // "church" or "add"
    var editing by remember { mutableStateOf<Any?>(null) }            // item being edited (null = new)
    var toDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val title = when (section) {
        "info" -> "Church Information"
        "ann" -> "Announcements"
        "events" -> "Events"
        else -> "Word of Life"
    }
    val collection = when (section) {
        "info" -> "information"
        "ann" -> "announcements"
        "events" -> "events"
        else -> "words"
    }

    fun save(id: String?, data: Map<String, Any>) {
        scope.launch {
            try {
                ChurchRepository.saveItem(collection, id, data)
                message = null
            } catch (e: Exception) {
                message = e.localizedMessage
            }
        }
    }

    Box(Modifier.fillMaxSize().background(ScreenBg)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title, church?.name ?: "", onBack = onBack)

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                message?.let { item { Text(it, color = Color.Red, fontSize = 13.sp) } }

                when (section) {
                    "info" -> {
                        item {
                            InfoCard(
                                church?.name ?: "Church profile not set up yet",
                                church?.let { "${it.location}\n${it.serviceTimes}\n${it.description}" }
                                    ?: "Tap the button below to add or edit the church name, location and service times.",
                                ChurchBlue
                            )
                        }
                        if (isPastor || isAdmin) {
                            item {
                                PrimaryButton(
                                    if (church == null) "Set up church profile" else "Edit church profile",
                                    onClick = { dialog = "church" }
                                )
                            }
                        }
                        item {
                            Text("Information posts (${infoPosts.size})", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        if (infoPosts.isEmpty()) {
                            item { Text("Nothing saved yet. Tap + to add information.", color = TextGray) }
                        }
                        items(infoPosts, key = { it.id }) { p ->
                            InfoCard(p.title, p.details, ChurchBlue,
                                authorName = p.authorName,
                                createdAt = p.createdAt,
                                onClick = { editing = p; dialog = "add" },
                                onDelete = { toDelete = collection to p.id })
                        }
                    }
                    "ann" -> {
                        if (announcements.isEmpty()) {
                            item { Text("No announcements yet. Tap + to add one.", color = TextGray) }
                        }
                        items(announcements, key = { it.id }) { a ->
                            InfoCard(a.title, a.message, ChurchGold,
                                tag = a.category,
                                authorName = a.authorName,
                                createdAt = a.createdAt,
                                onClick = { editing = a; dialog = "add" },
                                onDelete = { toDelete = collection to a.id })
                        }
                    }
                    "events" -> {
                        if (events.isEmpty()) {
                            item { Text("No events yet. Tap + to add one.", color = TextGray) }
                        }
                        items(events, key = { it.id }) { e ->
                            InfoCard(e.title, "${e.date}\n${e.description}", ChurchGreen,
                                authorName = e.authorName,
                                createdAt = e.createdAt,
                                onClick = { editing = e; dialog = "add" },
                                onDelete = { toDelete = collection to e.id })
                        }
                    }
                    else -> {
                        item {
                            Text(
                                if (isPastor) "Members see each word of faith in the Word tab at the date and time you set."
                                else "Only the Pastor is authorized to write and share the Word of Faith.",
                                color = if (isPastor) TextGray else Color.Red, fontSize = 13.sp
                            )
                        }
                        if (words.isEmpty()) {
                            item { Text("No words yet.", color = TextGray) }
                        }
                        items(words, key = { it.id }) { w ->
                            val upcoming = w.publishAt > now
                            val status = if (upcoming) "Scheduled for ${formatDateTime(w.publishAt)}"
                            else "Published ${formatDateTime(w.publishAt)}"
                            InfoCard(
                                w.title, "${w.scripture}\n${w.message}\n$status",
                                if (upcoming) ChurchGold else ChurchGreen,
                                tag = if (upcoming) "Scheduled" else "Published",
                                onClick = if (isPastor) ({ editing = w; dialog = "add" }) else null,
                                onDelete = if (isPastor) ({ toDelete = collection to w.id }) else null
                            )
                        }
                    }
                }
            }
        }

        if (section != "words" || isPastor) {
            FloatingActionButton(
                onClick = { editing = null; dialog = "add" },
                containerColor = ChurchGold,
                contentColor = ChurchBlueDark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) { Icon(Icons.Default.Add, "Add") }
        }
    }

    //church profile form (admin)
    if (dialog == "church") {
        val c = church
        FormDialog(
            "Church profile",
            listOf("Church name", "Location", "Service times", "Description"),
            listOf(c?.name ?: "", c?.location ?: "", c?.serviceTimes ?: "", c?.description ?: ""),
            { dialog = null }
        ) { v ->
            scope.launch {
                try {
                    ChurchRepository.saveChurch(v[0], v[1], v[2], v[3])
                    message = null
                } catch (e: Exception) {
                    message = e.localizedMessage
                }
            }
            dialog = null
        }
    }

    //add / edit form for this section
    if (dialog == "add") {
        when (section) {
            "info" -> {
                val p = editing as? InfoPost
                FormDialog(
                    if (p == null) "New information" else "Edit information",
                    listOf("Title", "Details"), listOf(p?.title ?: "", p?.details ?: ""), { dialog = null }
                ) { v ->
                    save(p?.id, mapOf("title" to v[0], "details" to v[1]))
                    dialog = null
                }
            }
            "ann" -> {
                val a = editing as? Announcement
                FormDialog(
                    if (a == null) "New announcement or notice" else "Edit announcement / notice",
                    listOf("Title", "Message"), listOf(a?.title ?: "", a?.message ?: ""),
                    { dialog = null },
                    choiceTitle = "Category / Tag",
                    choices = ANNOUNCEMENT_CATEGORIES,
                    initialChoice = a?.category ?: "Notice"
                ) { v ->
                    save(a?.id, mapOf("title" to v[0], "message" to v[1], "category" to v[2]))
                    dialog = null
                }
            }
            "events" -> {
                val e = editing as? Event
                FormDialog(
                    if (e == null) "New event" else "Edit event",
                    listOf("Title", "Date and time", "Description"),
                    listOf(e?.title ?: "", e?.date ?: "", e?.description ?: ""), { dialog = null }
                ) { v ->
                    save(e?.id, mapOf("title" to v[0], "date" to v[1], "description" to v[2]))
                    dialog = null
                }
            }
            else -> {
                val w = editing as? Word
                val whenMs = w?.publishAt ?: System.currentTimeMillis()
                FormDialog(
                    if (w == null) "New word of encouragement" else "Edit word",
                    listOf(
                        "Title",
                        "Scripture",
                        "Message",
                        "Date to share",
                        "Time to share"
                    ),
                    listOf(w?.title ?: "", w?.scripture ?: "", w?.message ?: "", formatDate(whenMs), formatTime(whenMs)),
                    { dialog = null },
                    validate = { v ->
                        if (parseDateTime(v[3], v[4]) == null) {
                            "Use the date format 2026-10-18 and the time format 06:30."
                        } else {
                            null
                        }
                    }
                ) { v ->
                    val at = parseDateTime(v[3], v[4]) ?: System.currentTimeMillis()
                    scope.launch {
                        try {
                            ChurchRepository.saveWord(w?.id, v[0], v[1], v[2], at)
                            message = null
                        } catch (e: Exception) {
                            message = e.localizedMessage
                        }
                    }
                    dialog = null
                }
            }
        }
    }

    //delete confirmation
    toDelete?.let { (col, id) ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = Color.White,
            title = { Text("Delete this item?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            ChurchRepository.deleteItem(col, id)
                        } catch (e: Exception) {
                            message = e.localizedMessage
                        }
                    }
                    toDelete = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel", color = TextGray) } }
        )
    }
}

//MEMBERS (pastor sees; admin can change roles)

@Composable
fun MembersScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val members by remember { ChurchRepository.members() }.collectAsState(initial = emptyList())
    val attendance by remember { ChurchRepository.attendance() }.collectAsState(initial = emptyList())
    val counts = adultAttendanceCounts(attendance)
    val role = rememberRole()
    val isStaff = isStaffRole(role)
    val isAdmin = role == "admin"
    val myUid = ChurchRepository.currentUid()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val filteredList = filterMembers(members, activeFilter)

    Box(Modifier.fillMaxSize().background(ScreenBg)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                "Members",
                "${members.size} registered",
                onBack = onBack
            )

            // Smart Search Bar
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            activeFilter = it
                        },
                        placeholder = { Text("Type member name or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; activeFilter = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { activeFilter = searchQuery },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChurchBlue)
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }

                ChurchRepository.lastError?.let {
                    Text("Could not load members: $it", color = Color.Red, fontSize = 13.sp)
                }
                message?.let {
                    Text(it, color = Color.Red, fontSize = 13.sp)
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item { Text(if (activeFilter.isBlank()) "No members registered yet." else "No members matching '$activeFilter'.", color = TextGray) }
                }
                items(filteredList, key = { it.uid }) { m ->
                    val details = buildString {
                        if (m.phone.isNotBlank()) append("Phone: ${m.phone}\n")
                        append(if (m.email.isNotBlank()) "Email: ${m.email}\n" else "Email: (Not provided - Manual Member)\n")
                        append("Attended ${counts[m.uid] ?: 0} service(s)")
                        if (m.role != "member") append("\nRole: ${m.role.replaceFirstChar { it.uppercase() }}")
                    }
                    val onTap: (() -> Unit)? =
                        if (isStaff && m.uid != myUid) ({ selectedMember = m }) else null
                    InfoCard(
                        m.name.ifBlank { m.email.ifBlank { "Member" } }, details,
                        when (m.role) {
                            "admin" -> ChurchGold
                            "pastor" -> ChurchGreen
                            else -> ChurchBlue
                        },
                        tag = if (m.email.isBlank()) "Manual Member" else null,
                        onClick = onTap
                    )
                }
            }
        }

        if (isStaff) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ChurchGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) { Icon(Icons.Default.PersonAdd, "Register Member") }
        }
    }

    // Add Member Dialog (Manual Registration)
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color.White,
            title = { Text("Register Member", color = ChurchBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Register a church member with name and phone number. Email is optional.", fontSize = 12.sp, color = TextGray)
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email Address (Optional)") },
                        singleLine = true
                    )
                    err?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            err = "Please enter both full name and phone number."
                        } else {
                            scope.launch {
                                try {
                                    ChurchRepository.addMember(name, phone, email)
                                    message = "Registered member: $name"
                                    showAddDialog = false
                                } catch (e: Exception) {
                                    err = e.localizedMessage
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChurchGreen)
                ) { Text("Register") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextGray) } }
        )
    }

    // Member Options Dialog (Role Change or Delete Manual Member)
    selectedMember?.let { m ->
        AlertDialog(
            onDismissRequest = { selectedMember = null },
            containerColor = Color.White,
            title = { Text(m.name.ifBlank { "Member Options" }, color = ChurchBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Role: ${m.role}", color = TextGray)
                    if (isAdmin) {
                        Text("Change Role:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        listOf("member" to "Member", "pastor" to "Pastor", "admin" to "Admin").forEach { (r, label) ->
                            OutlinedButton(
                                enabled = m.role != r,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        try {
                                            ChurchRepository.setRole(m.uid, r)
                                            message = "Role updated for ${m.name}"
                                        } catch (e: Exception) {
                                            message = e.localizedMessage
                                        }
                                    }
                                    selectedMember = null
                                }
                            ) { Text(label) }
                        }
                    }
                    if (isStaff && m.email.isBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        ChurchRepository.deleteMember(m.uid)
                                        message = "Removed member: ${m.name}"
                                    } catch (e: Exception) {
                                        message = e.localizedMessage
                                    }
                                }
                                selectedMember = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Remove Manual Member Record", color = Color.White) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { selectedMember = null }) { Text("Cancel", color = TextGray) } }
        )
    }
}

//ATTENDANCE & REPORTS (pastor / admin)

@Composable
fun AttendanceScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val members by remember { ChurchRepository.members() }.collectAsState(initial = emptyList())
    val attendance by remember { ChurchRepository.attendance() }.collectAsState(initial = emptyList())

    val role = rememberRole()
    val isStaff = isStaffRole(role)

    var tabIndex by remember { mutableIntStateOf(0) } // 0 = Mark Attendance, 1 = Reports
    var date by remember { mutableStateOf(todayString()) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("") }
    val present = remember { mutableStateListOf<String>() }
    var message by remember { mutableStateOf<String?>(null) }
    var reportDetailRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var toDeleteRecord by remember { mutableStateOf<AttendanceRecord?>(null) }

    // Load saved list for chosen date
    LaunchedEffect(date, attendance) {
        present.clear()
        present.addAll(attendance.firstOrNull { it.id == date }?.presentUids ?: emptyList())
    }

    fun toggle(uid: String) {
        if (uid in present) present.remove(uid) else present.add(uid)
    }

    val filteredMembers = filterMembers(members, activeFilter)
    val validDate = isValidDate(date)

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader("Attendance & Reports", "Mark attendance and view service reports.", onBack = onBack)

        // Mode switch tabs
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.White,
            contentColor = ChurchBlue
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text("Mark Attendance", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text("Attendance Reports (${attendance.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (tabIndex == 0) {
            // ----- TAB 0: MARK ATTENDANCE -----
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    date, { date = it.trim() },
                    label = { Text("Service Date (yyyy-MM-dd)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = date == todayString(),
                        onClick = { date = todayString() },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ChurchBlue, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = present.size == members.size && members.isNotEmpty(),
                        onClick = {
                            if (present.size == members.size) present.clear()
                            else {
                                present.clear()
                                present.addAll(members.map { it.uid })
                            }
                        },
                        label = { Text(if (present.size == members.size && members.isNotEmpty()) "Clear All" else "Mark All Present") }
                    )
                }

                // Smart Search Bar with Search Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            activeFilter = it
                        },
                        placeholder = { Text("Type member name or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; activeFilter = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { activeFilter = searchQuery },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChurchBlue)
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    "Present: ${present.size} of ${members.size} members",
                    fontWeight = FontWeight.Bold, color = TextDark
                )
                message?.let {
                    Text(
                        it, fontSize = 13.sp,
                        color = if (it.startsWith("Attendance saved")) ChurchGreen else Color.Red
                    )
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMembers.isEmpty()) {
                    item { Text(if (activeFilter.isBlank()) "No members registered yet." else "No members matching '$activeFilter'.", color = TextGray) }
                }
                items(filteredMembers, key = { it.uid }) { m ->
                    Card(
                        Modifier.fillMaxWidth().clickable { toggle(m.uid) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = m.uid in present,
                                onCheckedChange = { toggle(m.uid) },
                                colors = CheckboxDefaults.colors(checkedColor = ChurchGreen)
                            )
                            Column {
                                Text(m.name.ifBlank { m.email.ifBlank { "Member" } }, fontWeight = FontWeight.Bold, color = TextDark)
                                if (m.phone.isNotBlank()) Text("Phone: ${m.phone}", fontSize = 12.sp, color = TextGray)
                            }
                        }
                    }
                }
            }

            val context = LocalContext.current
            PrimaryButton(
                text = "Save Attendance",
                enabled = validDate,
                color = ChurchGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                onClick = {
                    scope.launch {
                        try {
                            ChurchRepository.saveAttendance(date, present.toList())
                            message = "Attendance saved for $date"
                        } catch (e: Exception) {
                            ChurchWorkManager.scheduleOfflineAttendanceSync(context, date, present.toList())
                            message = "Attendance queued for automatic background sync when internet connects."
                        }
                    }
                }
            )
        } else {
            //TAB 1: ATTENDANCE REPORTS
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                message?.let { item { Text(it, color = ChurchGreen, fontSize = 13.sp) } }
                if (attendance.isEmpty()) {
                    item { Text("No attendance records saved yet.", color = TextGray) }
                }
                items(attendance.sortedByDescending { it.date }, key = { it.id }) { rec ->
                    val presentCount = rec.presentUids.size
                    val totalMembers = members.size
                    val summaryText = formatAttendanceSummary(presentCount, totalMembers)

                    InfoCard(
                        title = "Service Date: ${rec.date}",
                        body = "Attendance: $summaryText\nTap to view present/absent member details",
                        accent = ChurchGold,
                        tag = "Report",
                        onClick = { reportDetailRecord = rec },
                        onDelete = if (isStaff) ({ toDeleteRecord = rec }) else null
                    )
                }
            }
        }
    }

    // Attendance Report Details Dialog
    reportDetailRecord?.let { rec ->
        val presentList = members.filter { it.uid in rec.presentUids }
        val absentList = members.filter { it.uid !in rec.presentUids }

        AlertDialog(
            onDismissRequest = { reportDetailRecord = null },
            containerColor = Color.White,
            title = { Text("Attendance Report: ${rec.date}", color = ChurchBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        formatAttendanceSummary(rec.presentUids.size, members.size),
                        fontWeight = FontWeight.Bold, color = ChurchGreen
                    )

                    Text("Present Members (${presentList.size}):", fontWeight = FontWeight.Bold, color = TextDark)
                    if (presentList.isEmpty()) {
                        Text("None marked present.", fontSize = 12.sp, color = TextGray)
                    } else {
                        presentList.forEach { m ->
                            Text("• ${m.name.ifBlank { m.email }}" + if (m.phone.isNotBlank()) " (${m.phone})" else "", fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Absent Members (${absentList.size}):", fontWeight = FontWeight.Bold, color = TextDark)
                    if (absentList.isEmpty()) {
                        Text("All registered members attended!", fontSize = 12.sp, color = ChurchGreen)
                    } else {
                        absentList.forEach { m ->
                            Text("• ${m.name.ifBlank { m.email }}" + if (m.phone.isNotBlank()) " (${m.phone})" else "", fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            },
            confirmButton = {
                if (isStaff) {
                    TextButton(
                        onClick = {
                            val recToDelete = reportDetailRecord
                            reportDetailRecord = null
                            toDeleteRecord = recToDelete
                        }
                    ) { Text("Delete Report", color = Color.Red, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = { TextButton(onClick = { reportDetailRecord = null }) { Text("Close", color = ChurchBlue) } }
        )
    }

    // Delete Attendance Report Confirmation Dialog
    toDeleteRecord?.let { rec ->
        AlertDialog(
            onDismissRequest = { toDeleteRecord = null },
            containerColor = Color.White,
            title = { Text("Delete Attendance Report?", color = ChurchBlue, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the attendance report for service date ${rec.date}? This action cannot be undone.", color = TextDark) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ChurchRepository.deleteAttendance(rec.id)
                                message = "Deleted attendance report for ${rec.date}"
                            } catch (e: Exception) {
                                message = e.localizedMessage
                            }
                        }
                        toDeleteRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { toDeleteRecord = null }) { Text("Cancel", color = TextGray) } }
        )
    }
}

// MEMBER QUESTIONS & INQUIRIES DASHBOARD (Pastor / Admin)

@Composable
fun PendingQuestionsScreen(
    onBack: () -> Unit,
    onOpenDetail: (String, String) -> Unit
) {
    val questions by remember { ChurchRepository.pendingQuestions() }.collectAsState(initial = emptyList())
    val unanswered = unansweredCount(questions)

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader(
            title = "Member Inquiries",
            subtitle = if (unanswered > 0) "$unanswered question(s) awaiting response" else "All member questions and inquiries",
            onBack = onBack
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (questions.isEmpty()) {
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
                            Icon(Icons.Default.CheckCircleOutline, null, tint = ChurchGreen, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No pending member questions", fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Questions asked by members on announcements or events will appear here for quick response.", fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            }
            items(questions, key = { "${it.collection}_${it.commentId}" }) { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (q.hasStaffReply) ChurchGreen.copy(alpha = 0.15f) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    if (q.hasStaffReply) "Answered" else "Needs Response",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (q.hasStaffReply) ChurchGreen else Color(0xFFD32F2F),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(formatDateTime(q.createdAt), fontSize = 11.sp, color = TextGray)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("Question by ${q.authorName}:", fontWeight = FontWeight.Bold, color = ChurchBlue, fontSize = 13.sp)
                        Text("\"${q.text}\"", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium)

                        Spacer(Modifier.height(8.dp))
                        Text("On: ${q.itemTitle} (${q.collection.replaceFirstChar { it.uppercase() }})", fontSize = 12.sp, color = TextGray)

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onOpenDetail(q.collection, q.itemId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (q.hasStaffReply) ChurchBlue else ChurchGold)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (q.hasStaffReply) "View Thread" else "Reply Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
