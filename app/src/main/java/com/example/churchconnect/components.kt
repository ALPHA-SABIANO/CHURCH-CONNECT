@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.churchconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//HELPERS

val ANNOUNCEMENT_CATEGORIES = listOf("Service", "Activity", "Notice")

fun formatDate(ms: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ms))
fun formatTime(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
fun formatDateTime(ms: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ms))
fun todayString(): String = formatDate(System.currentTimeMillis())

/** Turns "2026-10-18" + "06:30" into milliseconds, or null if the format is wrong. */
fun parseDateTime(date: String, time: String): Long? = try {
    val f = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    f.isLenient = false
    f.parse("${date.trim()} ${time.trim()}")?.time
} catch (e: Exception) {
    null
}

/** The logged-in user's role: "member", "pastor" or "admin". */
@Composable
fun rememberRole(): String {
    val uid = ChurchRepository.currentUid()
    val roleState by remember(uid) { ChurchRepository.userRoleFlow(uid) }.collectAsState(initial = "member")
    return roleState.lowercase().trim()
}

/** Current time, refreshed every 30 seconds so scheduled words appear on time. */
@Composable
fun rememberNow(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = System.currentTimeMillis()
        }
    }
    return now
}

//REUSABLE PIECES

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String = "",
    onBack: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    nextLabel: String = "Next",
    onNotificationClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ChurchBlueDark, ChurchBlue)))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f).padding(vertical = 4.dp)) {
                    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
                if (onNotificationClick != null) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Default.Notifications, "Notification Center", tint = ChurchGold)
                    }
                }
                if (onNext != null) {
                    TextButton(onClick = onNext) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(nextLabel, color = ChurchGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, nextLabel, tint = ChurchGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                action?.invoke()
            }
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(ChurchGold))
    }
}

@Composable
fun AppTabs(selected: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selected,
        containerColor = Color.White,
        contentColor = ChurchBlue
    ) {
        labels.forEachIndexed { i, label ->
            Tab(
                selected = selected == i,
                onClick = { onSelect(i) },
                selectedContentColor = ChurchBlue,
                unselectedContentColor = TextGray,
                text = {
                    Text(
                        label, fontSize = 12.sp,
                        fontWeight = if (selected == i) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    color: Color = ChurchBlue
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun OptionCard(
    icon: ImageVector, title: String, subtitle: String,
    accent: Color = ChurchBlue, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = TextGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
        }
    }
}

@Composable
fun InfoCard(
    title: String, body: String, accent: Color = ChurchBlue,
    tag: String? = null,
    authorName: String? = null,
    createdAt: Long? = null,
    onClick: (() -> Unit)? = null, onDelete: (() -> Unit)? = null
) {
    Card(
        Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(accent))
            Row(Modifier.weight(1f).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (!tag.isNullOrBlank()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.18f)) {
                            Text(
                                tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(title, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(body, fontSize = 13.sp, color = TextGray)
                    if (!authorName.isNullOrBlank() || (createdAt != null && createdAt > 0L)) {
                        Spacer(Modifier.height(6.dp))
                        val authorText = (authorName ?: "").ifBlank { "Admin" }
                        val timeText = if (createdAt != null && createdAt > 0L) formatDateTime(createdAt) else ""
                        val footer = if (timeText.isNotBlank()) "Shared by $authorText • $timeText" else "Shared by $authorText"
                        Text(footer, fontSize = 11.sp, color = TextGray)
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                }
            }
        }
    }
}

/** A row of selectable chips, used to filter announcements by category. */
@Composable
fun FilterRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ChurchBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * A pop-up form. If [choices] is given, a row of chips is shown and the chosen
 * option is added as the LAST item of the list passed to [onSave].
 */
@Composable
fun FormDialog(
    title: String,
    labels: List<String>,
    initial: List<String>,
    onDismiss: () -> Unit,
    choiceTitle: String = "",
    choices: List<String> = emptyList(),
    initialChoice: String = "",
    validate: (List<String>) -> String? = { null },
    onSave: (List<String>) -> Unit
) {
    val values = remember { mutableStateListOf(*initial.toTypedArray()) }
    var choice by remember {
        mutableStateOf(if (initialChoice in choices) initialChoice else choices.firstOrNull().orEmpty())
    }
    var err by remember { mutableStateOf<String?>(null) }

    fun result(): List<String> = if (choices.isEmpty()) values.toList() else values.toList() + choice

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, color = ChurchBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEachIndexed { i, label ->
                    OutlinedTextField(values[i], { values[i] = it }, label = { Text(label) })
                }
                if (choices.isNotEmpty()) {
                    Text(choiceTitle, fontSize = 12.sp, color = TextGray)
                    FilterRow(choices, choice) { choice = it }
                }
                err?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val all = result()
                    val problem = validate(all)
                    if (problem != null) {
                        err = problem
                    } else {
                        onSave(all)
                    }
                },
                enabled = values.first().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGreen)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) } }
    )
}

@Composable
fun WordCard(w: Word, subtitle: String = "Word of Today") {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(ChurchGold))
            Column(Modifier.padding(16.dp)) {
                if (subtitle.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = ChurchGold.copy(alpha = 0.20f)) {
                        Text(
                            subtitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChurchBlueDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(w.title, fontWeight = FontWeight.Bold, color = ChurchBlue, fontSize = 16.sp)
                if (w.scripture.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        w.scripture, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic,
                        color = ChurchBlueDark, fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(w.message, color = TextDark, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Shared by ${w.authorName.ifBlank { "the Pastor" }} • ${formatDateTime(w.publishAt)}",
                    color = TextGray, fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CommentCard(c: Comment, canDelete: Boolean, onDelete: () -> Unit, onReply: (() -> Unit)? = null) {
    val staffAuthor = c.authorRole == "pastor" || c.authorRole == "admin"
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (staffAuthor) SoftBlue else Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        c.authorName.ifBlank { "Member" },
                        fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp
                    )
                    if (staffAuthor) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (c.authorRole == "pastor") ChurchGreen else ChurchGold,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (c.authorRole == "pastor") "Pastor Reply" else "Admin Reply",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Text(formatDateTime(c.createdAt), fontSize = 11.sp, color = TextGray)
                Spacer(Modifier.height(4.dp))
                Text(c.text, color = TextDark, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onReply != null) {
                    TextButton(onClick = onReply, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                        Text("Reply", fontSize = 12.sp, color = ChurchBlue, fontWeight = FontWeight.Bold)
                    }
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                }
            }
        }
    }
}