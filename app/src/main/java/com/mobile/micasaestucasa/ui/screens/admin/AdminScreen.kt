package com.mobile.micasaestucasa.ui.screens.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.admin.ActionedUser
import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.UserReport
import com.mobile.micasaestucasa.domain.model.user.UserStatus
import com.mobile.micasaestucasa.ui.theme.Badges
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.Caution
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.SecondaryText
import com.mobile.micasaestucasa.ui.viewmodels.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProperty: (String) -> Unit = {},
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Dashboard" to Icons.Rounded.Dashboard,
        "Reports" to Icons.Rounded.Flag,
        "Keywords" to Icons.Rounded.Tag,
        "Users" to Icons.Rounded.People
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadAll(currentUserId) }

    // Refresh data when specific tabs are (re)selected
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.refreshReports(currentUserId)
        if (selectedTab == 3) viewModel.refreshActionedUsers()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Panel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeadingText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = Primario,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primario
                    )
                }
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    icon,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == index) Primario else CaptionLabels
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = if (selectedTab == index) Primario else CaptionLabels
                                )
                            }
                        }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> DashboardTab(stats = uiState.stats, reportCount = uiState.reports.size)
                1 -> ReportsTab(
                    reports = uiState.reports,
                    userNames = uiState.userNames,
                    propertyTitles = uiState.propertyTitles,
                    actionInProgress = uiState.actionInProgress,
                    onNavigateToProperty = onNavigateToProperty,
                    onSuspend = { userId, reportId ->
                        viewModel.suspendUser(userId, currentUserId, reportId)
                    },
                    onBan = { userId, reportId ->
                        viewModel.banUser(userId, currentUserId, reportId)
                    },
                    onDismiss = { reportId ->
                        viewModel.dismissReport(reportId, currentUserId)
                    }
                )
                2 -> KeywordsTab(
                    keywords = uiState.keywords,
                    onAdd = { label -> viewModel.addKeyword(label, currentUserId) },
                    onDelete = { id -> viewModel.deleteKeyword(id, currentUserId) },
                    onUpdate = { id, newLabel -> viewModel.updateKeyword(id, newLabel, currentUserId) }
                )
                3 -> UserManagementTab(
                    actionedUsers = uiState.actionedUsers,
                    reactivateInProgress = uiState.reactivateInProgress,
                    onReactivate = { userId -> viewModel.reactivateUser(userId, currentUserId) }
                )
            }
        }
    }
}

// ──────────────────────────────── DASHBOARD TAB ────────────────────────────────

@Composable
private fun DashboardTab(stats: BookingStats?, reportCount: Int) {
    if (stats == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = Primario)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Bookings Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
        }
        item {
            StatCardLarge(
                value = stats.total.toString(),
                label = "Total Bookings",
                icon = Icons.Rounded.CalendarMonth,
                color = Primario
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSmall(stats.completed.toString(), "Completed", Badges, Modifier.weight(1f))
                StatCardSmall(stats.active.toString(), "Active", Secondary, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSmall(stats.pending.toString(), "Pending", Caution, Modifier.weight(1f))
                StatCardSmall(stats.cancelled.toString(), "Cancelled", CaptionLabels, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSmall(stats.rejected.toString(), "Rejected", ErrorColor, Modifier.weight(1f))
                StatCardSmall(reportCount.toString(), "Open Reports", Caution, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCardLarge(value: String, label: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.15f)) {
            Icon(icon, null, tint = color, modifier = Modifier.padding(12.dp).size(28.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = color)
            Text(label, fontSize = 14.sp, color = SecondaryText)
        }
    }
}

@Composable
private fun StatCardSmall(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .padding(16.dp)
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
        Text(label, fontSize = 12.sp, color = CaptionLabels)
    }
}

@Composable
private fun ReportsTab(
    reports: List<UserReport>,
    userNames: Map<String, String>,
    propertyTitles: Map<String, String>,
    actionInProgress: Set<String>,
    onNavigateToProperty: (String) -> Unit,
    onSuspend: (userId: String, reportId: String) -> Unit,
    onBan: (userId: String, reportId: String) -> Unit,
    onDismiss: (reportId: String) -> Unit
) {
    var showBanDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    showBanDialog?.let { (userId, reportId) ->
        AlertDialog(
            onDismissRequest = { showBanDialog = null },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Confirm Permanent Ban", fontWeight = FontWeight.Bold, color = ErrorColor) },
            text = { Text("This action is irreversible. The user will be permanently banned.", color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = { onBan(userId, reportId); showBanDialog = null }) {
                    Text("Ban User", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = null }) {
                    Text("Cancel", color = CaptionLabels)
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("${reports.size} Pending Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
        }

        if (reports.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Secondary, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("All clear — no pending reports", color = CaptionLabels, fontSize = 14.sp)
                    }
                }
            }
        }

        items(reports, key = { it.id }) { report ->
            val inProgress = actionInProgress.contains(report.id)
            ReportCard(
                report = report,
                reportedUserName = userNames[report.reportedUserId] ?: report.reportedUserId.take(8) + "…",
                reporterName = userNames[report.reporterId] ?: report.reporterId.take(8) + "…",
                propertyTitle = report.propertyId?.let { propertyTitles[it] },
                inProgress = inProgress,
                onPropertyClick = { report.propertyId?.let { onNavigateToProperty(it) } },
                onSuspend = { onSuspend(report.reportedUserId, report.id) },
                onBan = { showBanDialog = report.reportedUserId to report.id },
                onDismiss = { onDismiss(report.id) }
            )
        }
    }
}

@Composable
private fun ReportCard(
    report: UserReport,
    reportedUserName: String,
    reporterName: String,
    propertyTitle: String?,
    inProgress: Boolean,
    onPropertyClick: () -> Unit,
    onSuspend: () -> Unit,
    onBan: () -> Unit,
    onDismiss: () -> Unit
) {
    val alpha by animateFloatAsState(if (inProgress) 0.5f else 1f, label = "alpha")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface.copy(alpha = alpha))
            .padding(16.dp)
    ) {
        // Header row: reported user + PENDING badge
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Reported User", fontSize = 11.sp, color = CaptionLabels)
                Text(
                    reportedUserName,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingText,
                    fontSize = 15.sp
                )
            }
            Surface(shape = RoundedCornerShape(50.dp), color = Caution.copy(alpha = 0.15f)) {
                Text(
                    "PENDING",
                    fontSize = 10.sp,
                    color = Caution,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Reporter info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Flag, null, tint = CaptionLabels, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Reported by: $reporterName", fontSize = 12.sp, color = CaptionLabels)
        }

        Spacer(Modifier.height(10.dp))

        // Reason chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ErrorColor.copy(alpha = 0.08f)
        ) {
            Text(
                report.reason,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ErrorColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // Description if present
        if (report.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                report.description,
                fontSize = 13.sp,
                color = SecondaryText,
                maxLines = 3
            )
        }

        // Property context if present — clickable to navigate
        report.propertyId?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPropertyClick() }
                    .background(Primario.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.Dashboard, null, tint = Primario, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    propertyTitle ?: "Loading…",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primario,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }

        // Timestamp
        Spacer(Modifier.height(6.dp))
        val dateStr = remember(report.createdAt) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(report.createdAt))
        }
        Text(dateStr, fontSize = 11.sp, color = CaptionLabels)

        Spacer(Modifier.height(14.dp))

        if (inProgress) {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(color = Primario, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else {
            // Action buttons: Dismiss | Suspend | Ban
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CaptionLabels),
                    border = BorderStroke(1.dp, CaptionLabels)
                ) {
                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Dismiss", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onSuspend,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Caution),
                    border = BorderStroke(1.dp, Caution)
                ) {
                    Icon(Icons.Rounded.PauseCircle, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Suspend", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
                Button(
                    onClick = onBan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Icon(Icons.Rounded.Gavel, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ban", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun KeywordsTab(
    keywords: List<Keyword>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit,
    onUpdate: (keywordId: String, newLabel: String) -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    // Edit dialog state: holds the keyword being edited
    var editingKeyword by remember { mutableStateOf<Keyword?>(null) }
    var editLabel by remember { mutableStateOf("") }

    // ── Add Dialog ──────────────────────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("New Keyword", fontWeight = FontWeight.Bold, color = HeadingText) },
            text = {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text("e.g. pool, wifi, kitchen") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        cursorColor = Primario
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newKeyword.isNotBlank()) {
                        onAdd(newKeyword.trim())
                        newKeyword = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add", color = Primario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = CaptionLabels)
                }
            }
        )
    }

    // ── Edit Dialog ──────────────────────────────────────────────────────────
    editingKeyword?.let { kw ->
        AlertDialog(
            onDismissRequest = { editingKeyword = null },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Edit Keyword", fontWeight = FontWeight.Bold, color = HeadingText) },
            text = {
                OutlinedTextField(
                    value = editLabel,
                    onValueChange = { editLabel = it },
                    label = { Text("New label") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        cursorColor = Primario
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editLabel.isNotBlank()) {
                        onUpdate(kw.id, editLabel.trim())
                        editingKeyword = null
                    }
                }) {
                    Text("Save", color = Primario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingKeyword = null }) {
                    Text("Cancel", color = CaptionLabels)
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${keywords.size} Keywords", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primario)
                        .size(40.dp)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = CardSurface)
                }
            }
        }
        items(keywords, key = { it.id }) { keyword ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Tag, null, tint = Primario, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(keyword.label, fontSize = 15.sp, color = HeadingText)
                }
                // Edit button
                IconButton(onClick = {
                    editLabel = keyword.label
                    editingKeyword = keyword
                }) {
                    Icon(Icons.Rounded.Edit, null, tint = Primario, modifier = Modifier.size(20.dp))
                }
                // Delete button
                IconButton(onClick = { onDelete(keyword.id) }) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = ErrorColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ──────────────────────────────── USERS TAB ────────────────────────────────────

@Composable
private fun UserManagementTab(
    actionedUsers: List<ActionedUser>,
    reactivateInProgress: Set<String>,
    onReactivate: (userId: String) -> Unit
) {
    var showReactivateDialog by remember { mutableStateOf<ActionedUser?>(null) }

    showReactivateDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showReactivateDialog = null },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Reactivate User",
                    fontWeight = FontWeight.Bold,
                    color = HeadingText
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to reactivate this user?",
                        color = SecondaryText
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${user.name.ifBlank { user.email }} will regain access and their ${user.propertiesOnHold} properties will be restored.",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onReactivate(user.id)
                    showReactivateDialog = null
                }) {
                    Text("Reactivate", color = Secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReactivateDialog = null }) {
                    Text("Cancel", color = CaptionLabels)
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "${actionedUsers.size} Actioned Users",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = HeadingText
            )
        }

        if (actionedUsers.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            null,
                            tint = Secondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No suspended or banned users",
                            color = CaptionLabels,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        items(actionedUsers, key = { it.id }) { user ->
            val inProgress = reactivateInProgress.contains(user.id)
            ActionedUserCard(
                user = user,
                inProgress = inProgress,
                onReactivate = { showReactivateDialog = user }
            )
        }
    }
}

@Composable
private fun ActionedUserCard(
    user: ActionedUser,
    inProgress: Boolean,
    onReactivate: () -> Unit
) {
    val alpha by animateFloatAsState(if (inProgress) 0.5f else 1f, label = "alpha")
    val isBanned = user.status == UserStatus.BANNED

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface.copy(alpha = alpha))
            .padding(16.dp)
    ) {
        // Header: user info + status badge
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar placeholder
                Surface(
                    shape = CircleShape,
                    color = if (isBanned) {
                        ErrorColor.copy(alpha = 0.12f)
                    } else {
                        Caution.copy(alpha = 0.12f)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Person,
                            null,
                            tint = if (isBanned) ErrorColor else Caution,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        user.name.ifBlank { "Unknown" },
                        fontWeight = FontWeight.SemiBold,
                        color = HeadingText,
                        fontSize = 15.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Email,
                            null,
                            tint = CaptionLabels,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            user.email.ifBlank { "No email" },
                            fontSize = 12.sp,
                            color = CaptionLabels
                        )
                    }
                }
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = if (isBanned) {
                    ErrorColor.copy(alpha = 0.15f)
                } else {
                    Caution.copy(alpha = 0.15f)
                }
            ) {
                Text(
                    if (isBanned) "BANNED" else "SUSPENDED",
                    fontSize = 10.sp,
                    color = if (isBanned) ErrorColor else Caution,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Properties on hold count
        if (user.propertiesOnHold > 0) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primario.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Rounded.Home,
                    null,
                    tint = Primario,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${user.propertiesOnHold} properties on hold",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primario
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Action area
        if (inProgress) {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(
                    color = Primario,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else if (isBanned) {
            // Banned users cannot be reactivated
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorColor.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Block,
                    null,
                    tint = ErrorColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Permanently banned — cannot be reactivated",
                    fontSize = 12.sp,
                    color = ErrorColor,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Suspended users can be reactivated
            Button(
                onClick = onReactivate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Reactivate User",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
