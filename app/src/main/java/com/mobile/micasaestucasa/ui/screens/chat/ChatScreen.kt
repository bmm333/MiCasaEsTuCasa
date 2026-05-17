package com.mobile.micasaestucasa.ui.screens.chat

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.mobile.micasaestucasa.ui.viewmodels.chat.ChatViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mobile.micasaestucasa.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    hostId: String,
    renterId: String,
    propertyId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val vmConversationId by viewModel.activeConversationId.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }


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

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Sfumatura),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                null,
                                tint = Primario,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentUserId == hostId) "Renter" else "Owner",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = HeadingText
                            )
                            Text("Online", fontSize = 11.sp, color = Secondary)
                        }
                    }
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
            ChatInputBar(text = inputText,
                onTextChange = { inputText = it },
                selectedImage = selectedImageUri,
                onImagePick = { imagePicker.launch("image/*") },
                onImageClear = { selectedImageUri = null },
                onSend = {
                    if (inputText.isNotBlank() || selectedImageUri != null) {
                        viewModel.sendMessage(
                            senderId = currentUserId,
                            text = inputText.trim()
                        )
                        inputText = ""
                        selectedImageUri = null
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
    val textColor   = if (isMine) CardSurface else HeadingText
    val alignment   = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine)
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    else
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)

    Column(
        modifier            = Modifier.fillMaxWidth(),
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
                    model              = message.imageUrl,
                    contentDescription = "Foto",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
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
                    text     = message.text,
                    color    = textColor,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text     = formatTime(message.timestamp),
            fontSize = 10.sp,
            color    = CaptionLabels,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}


@Composable
private fun ChatInputBar(
    text:String,
    onTextChange: (String) -> Unit,
    selectedImage: Uri?,
    onImagePick: () -> Unit,
    onImageClear: () -> Unit,
    onSend: () -> Unit
) {
    Surface(color=CardSurface, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if(selectedImage!=null)
            {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                ){
                    AsyncImage(
                        model=selectedImage,
                        contentDescription="Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(onClick = onImageClear,
                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(ErrorColor,CircleShape)){
                        Icon(Icons.Rounded.Close,null,tint=CardSurface, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(modifier=Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                //gallery vtton
                IconButton(onClick=onImagePick, modifier = Modifier.size(40.dp).clip(CircleShape).background(SkeletonLoader))
                {
                    Icon(Icons.Rounded.Image,null,tint=CaptionLabels, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier=Modifier.width(8.dp))
                //text inpt
                OutlinedTextField(
                    value         = text,
                    onValueChange = onTextChange,
                    placeholder   = { Text("Send a message", color = BorderDivider) },
                    shape         = RoundedCornerShape(24.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Primario,
                        unfocusedBorderColor    = BorderDivider,
                        focusedContainerColor   = ScreenBackground,
                        unfocusedContainerColor = ScreenBackground,
                        cursorColor             = Primario
                    ),
                    modifier    = Modifier.weight(1f),
                    maxLines    = 4,
                    singleLine  = false
                )
                Spacer(modifier=Modifier.width(8.dp))
                //send btn
                val canSend = text.isNotBlank() || selectedImage != null
                IconButton(
                    onClick  = onSend,
                    enabled  = canSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) Primario else SkeletonLoader)
                ){
                    Icon(
                        Icons.Rounded.Send, null,
                        tint     = if (canSend) CardSurface else CaptionLabels,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return SimpleDateFormat("HH:mm", Locale.ITALY).format(Date(timestamp))
}