package com.mobile.micasaestucasa.ui.screens.chat

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.MiCasaConnectedBottomNav
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Sfumatura
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatUiState
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    currentUserId: String,
    onNavigateToChat: (Conversation) -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversationUsers by viewModel.conversationUsers.collectAsState()
    val conversationProperties by viewModel.conversationProperties.collectAsState()
    val totalUnreadCount by viewModel.totalUnreadCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations(currentUserId)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Messages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
        },
        bottomBar = {
            MiCasaConnectedBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "messages_screen",
                onItemSelected = { route ->
                    when (route) {
                        "home_screen" -> navController.navigate(Route.Home) {
                            popUpTo(0)
                        }
                        "saved_screen" -> navController.navigate(Route.Wishlist)
                        "trips_screen" -> navController.navigate(Route.Trips)
                        "messages_screen" -> { /* already here */ }
                        "profile_screen" -> navController.navigate(Route.Profile)
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ChatUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primario)
                }
            }

            is ChatUiState.ConversationsLoaded -> {
                // Load user profiles for all conversations
                LaunchedEffect(state.conversations) {
                    viewModel.loadConversationUsers(state.conversations, currentUserId)
                }
                if (state.conversations.isEmpty()) {
                    EmptyConversationsView(Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.conversations) { conversation ->
                            val otherUserId = if (currentUserId == conversation.hostId) {
                                conversation.renterId
                            } else {
                                conversation.hostId
                            }
                            val otherUser = conversationUsers[otherUserId]
                            val propertyTitle = conversationProperties[conversation.propertyId]
                            ConversationItem(
                                conversation = conversation,
                                currentUserId = currentUserId,
                                otherUser = otherUser,
                                propertyTitle = propertyTitle,
                                onClick = { onNavigateToChat(conversation) }
                            )
                        }
                    }
                }
            }

            is ChatUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = ErrorColor)
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    otherUser: User?,
    propertyTitle: String?,
    onClick: () -> Unit
) {
    val isHost = currentUserId == conversation.hostId
    val displayName = if (otherUser != null) {
        "${otherUser.name} ${otherUser.lastName}".trim().ifBlank { "Utente" }
    } else {
        if (isHost) "Renter" else "Owner"
    }
    val photoUrl = otherUser?.profileImageUrl
    val hasUnread = conversation.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (hasUnread) CardSurface else CardSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // avatar — photo or initial letter
        Box {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Sfumatura),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Primario
                    )
                }
            }
            // Unread indicator dot on avatar
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0xFFE5474B))
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = HeadingText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    // Role chip: Host or Guest
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(if (isHost) Primario.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color(0xFF00A699).copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isHost) "Host" else "Ospite",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHost) Primario else androidx.compose.ui.graphics.Color(0xFF00A699)
                        )
                    }
                }
                Text(
                    text = formatTimestamp(conversation.lastMessageTimestamp),
                    fontSize = 11.sp,
                    color = if (hasUnread) Primario else CaptionLabels,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            // Property chip – prominent colored tag
            if (!propertyTitle.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .background(Primario.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = propertyTitle,
                        fontSize = 11.sp,
                        color = Primario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
            Text(
                text = conversation.lastMessage.ifBlank { "Nessun messaggio" },
                fontSize = 13.sp,
                color = if (hasUnread) HeadingText else CaptionLabels,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal
            )
        }
        if (hasUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Primario),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${conversation.unreadCount}",
                    fontSize = 10.sp,
                    color = CardSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 80.dp),
        color = BorderDivider,
        thickness = 0.5.dp
    )
}

@Composable
private fun EmptyConversationsView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.ChatBubbleOutline,
                null,
                tint = BorderDivider,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No conversations yet", color = CaptionLabels, fontSize = 16.sp)
            Text(
                "Your chat's will appear here",
                color = BorderDivider,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.ITALY)
    return sdf.format(Date(timestamp))
}
