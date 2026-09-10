package com.mobile.micasaestucasa.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Home
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.ui.components.report.ReportDialog
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.Sfumatura
import com.mobile.micasaestucasa.ui.theme.SkeletonLoader
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    hostId: String,
    renterId: String,
    propertyId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProperty: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val vmConversationId by viewModel.activeConversationId.collectAsState()
    val otherUserName by viewModel.otherUserName.collectAsState()
    val otherUserPhotoUrl by viewModel.otherUserPhotoUrl.collectAsState()
    val currentPropertyTitle by viewModel.currentPropertyTitle.collectAsState()
    val otherUserIsOnline by viewModel.otherUserIsOnline.collectAsState()
    val otherUserLastSeen by viewModel.otherUserLastSeen.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val activeConversationId = vmConversationId.ifBlank { conversationId }

    // picker of img
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    // Two entry paths:
    // 1) From PropertyDetail → hostId/renterId/propertyId are set → openConversation resolves the real id
    // 2) From ConversationList → only conversationId is set → observe directly
    LaunchedEffect(Unit) {
        if (hostId.isNotBlank() && renterId.isNotBlank() && propertyId.isNotBlank()) {
            viewModel.openConversation(hostId, renterId, propertyId)
        } else if (conversationId.isNotBlank()) {
            viewModel.openConversationById(conversationId)
        }
        // Load the other user's profile
        val otherUserId = if (currentUserId == hostId) renterId else hostId
        if (otherUserId.isNotBlank()) {
            viewModel.loadOtherUser(otherUserId)
            viewModel.setCurrentUserPresence(currentUserId, true)
        }
    }

    // Gestione presenza: online quando la schermata è visibile, offline quando va in background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentUserId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setCurrentUserPresence(currentUserId, true)
                Lifecycle.Event.ON_PAUSE  -> viewModel.setCurrentUserPresence(currentUserId, false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setCurrentUserPresence(currentUserId, false)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // marks as read when the conversation is ready
    LaunchedEffect(activeConversationId) {
        if (activeConversationId.isNotBlank()) {
            viewModel.markAsRead(activeConversationId, currentUserId)
        }
    }

    // Report dialog
    if (showReportDialog) {
        val reportedUser = if (currentUserId == hostId) renterId else hostId
        ReportDialog(
            reportedUserId = reportedUser,
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, description ->
                viewModel.reportUser(
                    reporterId = currentUserId,
                    reportedUserId = reportedUser,
                    reason = reason,
                    description = description,
                    propertyId = propertyId.takeIf { it.isNotBlank() }
                )
                showReportDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Report submitted — admins will review.")
                }
            }
        )
    }

    Scaffold(
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar con pallino presenza
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (!otherUserPhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = otherUserPhotoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Sfumatura),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = otherUserName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Primario
                                    )
                                }
                            }
                            // Pallino verde/grigio presenza
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(CardSurface)
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            if (otherUserIsOnline)
                                                androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                            else
                                                CaptionLabels
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            // Riga 1: Nome · Stato (inline compatto)
                            val presenceText = when {
                                otherUserIsOnline -> "Online"
                                otherUserLastSeen != null -> {
                                    val diffMs = System.currentTimeMillis() - otherUserLastSeen!!
                                    val diffMin = diffMs / 60_000
                                    val diffHrs = diffMin / 60
                                    val diffDays = diffHrs / 24
                                    when {
                                        diffMin < 1 -> "Visto poco fa"
                                        diffMin < 60 -> "Visto ${diffMin}m fa"
                                        diffHrs < 24 -> "Visto ${diffHrs}h fa"
                                        else -> "Visto ${diffDays}g fa"
                                    }
                                }
                                else -> "Offline"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = otherUserName.ifBlank {
                                        if (currentUserId == hostId) "Renter" else "Owner"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = HeadingText,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    "·",
                                    fontSize = 12.sp,
                                    color = CaptionLabels
                                )
                                Text(
                                    presenceText,
                                    fontSize = 11.sp,
                                    color = if (otherUserIsOnline)
                                        androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    else
                                        CaptionLabels,
                                    maxLines = 1
                                )
                            }
                            // Riga 2: Titolo casa (solo se disponibile)
                            if (currentPropertyTitle.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Home,
                                        contentDescription = null,
                                        tint = Primario,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        currentPropertyTitle,
                                        fontSize = 10.sp,
                                        color = Primario,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                },

                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Rounded.Flag, "Report user", tint = CaptionLabels)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        bottomBar = {
            val imageUploadState by viewModel.imageUploadState.collectAsState()
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                selectedImage = selectedImageUri,
                imageUploadState = imageUploadState,
                onImagePick = { imagePicker.launch("image/*") },
                onImageClear = { selectedImageUri = null },
                onSend = {
                    val convId = activeConversationId.ifBlank { return@ChatInputBar }
                    val uri = selectedImageUri

                    when {
                        // caso 1: solo testo
                        uri == null && inputText.isNotBlank() -> {
                            viewModel.sendMessage(
                                senderId = currentUserId,
                                text = inputText.trim()
                            )
                            inputText = ""
                        }
                        // caso 2: immagine (con o senza testo)
                        uri != null -> {
                            viewModel.sendMessageWithImage(
                                uri = uri,
                                conversationId = convId,
                                senderId = currentUserId,
                                text = inputText.trim()
                            )
                            inputText = ""
                            selectedImageUri = null
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (currentPropertyTitle.isNotBlank()) {
                item {
                    PropertyChatHeader(
                        title = currentPropertyTitle,
                        onClick = {
                            if (propertyId.isNotBlank()) {
                                onNavigateToProperty(propertyId)
                            }
                        }
                    )
                }
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    val bubbleColor = if (isMine) Primario else CardSurface
    val textColor = if (isMine) CardSurface else HeadingText
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!message.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = "Foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                if (message.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatTime(message.timestamp),
            fontSize = 10.sp,
            color = CaptionLabels,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedImage: Uri?,
    imageUploadState: com.mobile.micasaestucasa.ui.viewmodels.chat.ImageUploadState,
    onImagePick: () -> Unit,
    onImageClear: () -> Unit,
    onSend: () -> Unit
) {
    val isUploading = imageUploadState is com.mobile.micasaestucasa.ui.viewmodels.chat.ImageUploadState.Uploading

    Surface(color = CardSurface, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // preview immagine selezionata
            if (selectedImage != null) {
                Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // overlay caricamento
                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(HeadingText.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CardSurface,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        // bottone rimuovi — solo se non sta caricando
                        IconButton(
                            onClick = onImageClear,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .background(ErrorColor, CircleShape)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                null,
                                tint = CardSurface,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // errore upload
            if (imageUploadState is com.mobile.micasaestucasa.ui.viewmodels.chat.ImageUploadState.Error) {
                Text(
                    text = imageUploadState.message,
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // bottone galleria — disabilitato durante upload
                IconButton(
                    onClick = onImagePick,
                    enabled = !isUploading,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isUploading) BorderDivider else SkeletonLoader)
                ) {
                    Icon(
                        Icons.Rounded.Image,
                        null,
                        tint = if (isUploading) BorderDivider else CaptionLabels,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    enabled = !isUploading,
                    placeholder = {
                        Text(
                            if (isUploading) "Caricamento..." else "Scrivi un messaggio...",
                            color = BorderDivider
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        unfocusedBorderColor = BorderDivider,
                        focusedContainerColor = ScreenBackground,
                        unfocusedContainerColor = ScreenBackground,
                        cursorColor = Primario
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    singleLine = false
                )

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = (text.isNotBlank() || selectedImage != null) && !isUploading
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) Primario else SkeletonLoader)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = CardSurface,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            null,
                            tint = if (canSend) CardSurface else CaptionLabels,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return SimpleDateFormat("HH:mm", Locale.ITALY).format(Date(timestamp))
}

@Composable
private fun PropertyChatHeader(title: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
                .border(1.dp, Primario.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vedi $title",
                    color = Primario,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
