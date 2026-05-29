package com.mobile.micasaestucasa.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatUiState
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.navigation.NavController
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    currentUserId: String,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "messages_screen",
                onItemSelected = { route ->
                    when (route) {
                        "home_screen"     -> navController.navigate(Route.Home) {
                            popUpTo(0)
                        }
                        "saved_screen"    -> navController.navigate(Route.Wishlist)
                        "trips_screen"    -> navController.navigate(Route.Trips)
                        "messages_screen" -> { /* already here */ }
                        "profile_screen"  -> navController.navigate(Route.Profile)
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
                            ConversationItem(
                                conversation = conversation,
                                currentUserId = currentUserId,
                                onClick = { onNavigateToChat(conversation.id) }
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(CardSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // avatar place
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Sfumatura),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                tint = Primario,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentUserId == conversation.hostId) "Renter" else "Owner",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = HeadingText
                )
                Text(
                    text = formatTimestamp(conversation.lastMessageTimestamp),
                    fontSize = 11.sp,
                    color = CaptionLabels
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessage.ifBlank { "No Message" },
                fontSize = 13.sp,
                color = CaptionLabels,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (conversation.unreadCount > 0) {
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
                tint     = BorderDivider,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No conversations yet", color = CaptionLabels, fontSize = 16.sp)
            Text(
                "Your chat's will appear here",
                color    = BorderDivider,
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
