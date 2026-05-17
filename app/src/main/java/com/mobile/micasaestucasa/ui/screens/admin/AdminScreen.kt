package com.mobile.micasaestucasa.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    currentUserId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Statistics", "Keywords", "Users")

    LaunchedEffect(Unit) {
        viewModel.loadAll(currentUserId)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pannello Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = HeadingText
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = CardSurface,
                contentColor     = Primario,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primario
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize   = 13.sp,
                                fontWeight = if (selectedTab == index)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) Primario else CaptionLabels
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> StatsTab(stats = uiState.stats)
                1 -> KeywordsTab(
                    keywords = uiState.keywords,
                    onAdd    = { label -> viewModel.addKeyword(label, currentUserId) },
                    onDelete = { id -> viewModel.deleteKeyword(id, currentUserId) }
                )
                2 -> UsersTab(
                    reports    = uiState.reports,
                    onSuspend  = { userId -> viewModel.suspendUser(userId, currentUserId) },
                    onBan      = { userId -> viewModel.banUser(userId, currentUserId) }
                )
            }
        }
    }
}

@Composable
private fun StatsTab(stats: BookingStats?) {
    if (stats == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = Primario)
        }
        return
    }

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bookings Recap",
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
                color      = HeadingText
            )
        }
        item {
            StatCardLarge(
                value = stats.total.toString(),
                label = "Total Bookings",
                icon  = Icons.Rounded.CalendarMonth,
                color = Primario
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSmall(
                    value    = stats.completed.toString(),
                    label    = "Done",
                    color    = Badges,
                    modifier = Modifier.weight(1f)
                )
                StatCardSmall(
                    value    = stats.active.toString(),
                    label    = "On Going",
                    color    = Secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSmall(
                    value    = stats.pending.toString(),
                    label    = "Waiting",
                    color    = Caution,
                    modifier = Modifier.weight(1f)
                )
                StatCardSmall(
                    value    = stats.cancelled.toString(),
                    label    = "Canceled",
                    color    = CaptionLabels,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            StatCardSmall(
                value = stats.rejected.toString(),
                label = "Rejected",
                color = ErrorColor
            )
        }
    }
}

@Composable
private fun StatCardLarge(
    value: String,
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = color)
            Text(label, fontSize = 14.sp, color = SecondaryText)
        }
    }
}

@Composable
private fun StatCardSmall(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
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
private fun KeywordsTab(
    keywords: List<Keyword>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor   = CardSurface,
            shape            = RoundedCornerShape(20.dp),
            title = { Text("New Keyword", fontWeight = FontWeight.Bold, color = HeadingText) },
            text = {
                OutlinedTextField(
                    value         = newKeyword,
                    onValueChange = { newKeyword = it },
                    label         = { Text("Eg. pool, wifi, kitchen") },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        cursorColor        = Primario
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newKeyword.isNotBlank()) {
                        onAdd(newKeyword.trim())
                        newKeyword = ""
                        showDialog = false
                    }
                }) {
                    Text("Add", color = Primario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = CaptionLabels)
                }
            }
        )
    }

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "${keywords.size} keywords",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = HeadingText
                )
                IconButton(
                    onClick  = { showDialog = true },
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
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Tag, null,
                        tint     = Primario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(keyword.label, fontSize = 15.sp, color = HeadingText)
                }
                IconButton(onClick = { onDelete(keyword.id) }) {
                    Icon(
                        Icons.Rounded.DeleteOutline, null,
                        tint     = ErrorColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun UsersTab(
    reports: List<com.mobile.micasaestucasa.domain.model.admin.UserReport>,
    onSuspend: (String) -> Unit,
    onBan: (String) -> Unit
) {
    var showBanDialog by remember { mutableStateOf<String?>(null) }

    showBanDialog?.let { userId ->
        AlertDialog(
            onDismissRequest = { showBanDialog = null },
            containerColor   = CardSurface,
            shape            = RoundedCornerShape(20.dp),
            title = {
                Text("Confirm Ban", fontWeight = FontWeight.Bold, color = ErrorColor)
            },
            text = {
                Text(
                    "This action is permanent and cannot be undone.",
                    color = SecondaryText
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onBan(userId)
                    showBanDialog = null
                }) {
                    Text("Permanent Ban", color = ErrorColor, fontWeight = FontWeight.Bold)
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
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "${reports.size} on hold reports",
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
                color      = HeadingText
            )
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.CheckCircle, null,
                            tint     = Secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No reports found", color = CaptionLabels)
                    }
                }
            }
        }

        items(reports, key = { it.id }) { report ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "User Reported",
                            fontSize = 11.sp,
                            color    = CaptionLabels
                        )
                        Text(
                            report.reportedUserId.take(12) + "...",
                            fontWeight = FontWeight.SemiBold,
                            color      = HeadingText
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Caution.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "PENDING",
                            fontSize   = 10.sp,
                            color      = Caution,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(report.reason, fontSize = 13.sp, color = SecondaryText)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = { onSuspend(report.reportedUserId) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Caution),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Caution)
                    ) {
                        Text("Suspend", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Button(
                        onClick  = { showBanDialog = report.reportedUserId },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                    ) {
                        Text("Ban", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}