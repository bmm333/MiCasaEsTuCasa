package com.mobile.micasaestucasa.ui.viewmodels.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.usecase.admin.AddUserReportUseCase
import com.mobile.micasaestucasa.domain.usecase.chat.GetOrCreateConversationUseCase
import com.mobile.micasaestucasa.domain.usecase.chat.SendMessageUseCase
import com.mobile.micasaestucasa.domain.usecase.property.GetPropertyByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    private val chatRepo: ChatRepo,
    private val addUserReportUseCase: AddUserReportUseCase,
    private val userRepo: UserRepo,
    private val storageRepository: com.mobile.micasaestucasa.data.repository.storage.FirebaseStorageRepository,
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase
) : ViewModel() {
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _activeConversationId = MutableStateFlow("")
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    /** Name of the other user in the current 1:1 chat. */
    private val _otherUserName = MutableStateFlow("")
    val otherUserName: StateFlow<String> = _otherUserName.asStateFlow()

    /** Profile photo URL of the other user in the current 1:1 chat. */
    private val _otherUserPhotoUrl = MutableStateFlow<String?>(null)
    val otherUserPhotoUrl: StateFlow<String?> = _otherUserPhotoUrl.asStateFlow()

    /** Cached user profiles keyed by UID, used by the conversation list. */
    private val _conversationUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    val conversationUsers: StateFlow<Map<String, User>> = _conversationUsers.asStateFlow()

    /** Cached property titles keyed by propertyId, used by the conversation list. */
    private val _conversationProperties = MutableStateFlow<Map<String, String>>(emptyMap())
    val conversationProperties: StateFlow<Map<String, String>> = _conversationProperties.asStateFlow()

    /** Property title of the currently active chat. */
    private val _currentPropertyTitle = MutableStateFlow("")
    val currentPropertyTitle: StateFlow<String> = _currentPropertyTitle.asStateFlow()

    /** Total number of unread messages across all conversations for the current user.
     *  Drives the red badge on the Messages tab in the bottom nav. */
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    /**
     * Opens or creates the conversations and starts the observer
     *
     * @param hostId UID of the host
     * @param renterId UID of the renter
     * @param propertyId Id of the property
     * */
    fun openConversation(hostId: String, renterId: String, propertyId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            getOrCreateConversationUseCase(hostId, renterId, propertyId)
                .onSuccess { conversation ->
                    _uiState.value = ChatUiState.ConversationReady(conversation)
                    // use the REAL Firestore id for observing and sending
                    _activeConversationId.value = conversation.id
                    _currentConversationId.value = conversation.id
                    observeMessages(conversation.id)
                    // resolve property title for the chat top bar
                    if (propertyId.isNotBlank()) {
                        getPropertyByIdUseCase(propertyId)
                            .onSuccess { prop -> _currentPropertyTitle.value = prop.title }
                            .onFailure { _currentPropertyTitle.value = "" }
                    }
                }
                .onFailure {
                    _uiState.value = ChatUiState.Error(
                        it.message ?: "Error opening the conversation"
                    )
                }
        }
    }

    /**
     * Opens a conversation when we already know the Firestore conversation id
     * (e.g. coming from ConversationList). Sets the internal id and starts observing.
     * */
    fun openConversationById(conversationId: String) {
        _activeConversationId.value = conversationId
        _currentConversationId.value = conversationId
        observeMessages(conversationId)
        // resolve property title from the conversation doc
        viewModelScope.launch {
            chatRepo.getConversationsForUser(_activeConversationId.value)
            // We don't have a direct getConversationById; the title will be
            // populated by loadConversationUsers if navigating from ConversationList
        }
    }

    /**
     * observes messages in realtime through firesotre snapshotlisnter
     * collects is deleted automaitcally with viewmodelscope
     * */
    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            chatRepo.observeMessages(conversationId)
                .collect { messages ->
                    _messages.value = messages
                }
        }
    }

    // Holds the REAL Firestore conversation id resolved by openConversation
    private val _currentConversationId = MutableStateFlow<String?>(null)

    /**
     * sends a textual message using the REAL conversation id
     * resolved by openConversation — not the one from the route
     * @param senderId UID of the sender
     * @param text Text of the message
     * @param imageUrl optional image URL
     * */
    fun sendMessage(senderId: String, text: String, imageUrl: String? = null) {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            sendMessageUseCase(convId, senderId, text, imageUrl)
                .onFailure {
                    _uiState.value = ChatUiState.Error(
                        it.message ?: "Errore sending message"
                    )
                }
            // On success: do nothing to _uiState — the snapshot listener
            // will push the new message into _messages automatically.
        }
    }

    fun loadConversations(userId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            chatRepo.getConversationsForUser(userId)
                .onSuccess { conversations ->
                    _uiState.value = ChatUiState.ConversationsLoaded(conversations)
                    _totalUnreadCount.value = conversations.sumOf { it.unreadCount }
                }
                .onFailure {
                    _uiState.value = ChatUiState.Error(
                        it.message ?: "Errore loading the conversation"
                    )
                }
        }
    }

    fun markAsRead(conversationId: String, userId: String) {
        viewModelScope.launch {
            chatRepo.markMessagesAsRead(conversationId, userId)
        }
    }

    fun resetState() { _uiState.value = ChatUiState.Idle }

    /**
     * Loads the profile of the other user in a 1:1 chat.
     * Populates otherUserName and otherUserPhotoUrl.
     *
     * @param userId UID of the other participant.
     */
    fun loadOtherUser(userId: String) {
        viewModelScope.launch {
            userRepo.getUserById(userId)
                .onSuccess { user ->
                    if (user != null) {
                        _otherUserName.value = "${user.name} ${user.lastName}".trim()
                        _otherUserPhotoUrl.value = user.profileImageUrl
                    } else {
                        // utente eliminato — mostra placeholder
                        _otherUserName.value = "Utente eliminato"
                        _otherUserPhotoUrl.value = null
                    }
                }
                .onFailure {
                    // utente eliminato o errore — mostra placeholder
                    _otherUserName.value = "Utente eliminato"
                    _otherUserPhotoUrl.value = null
                }
        }
    }

    /**
     * After conversations are loaded, fetches the other user's profile
     * for each conversation and caches them in conversationUsers.
     *
     * @param conversations The list of loaded conversations.
     * @param currentUserId The UID of the logged-in user.
     */
    fun loadConversationUsers(
        conversations: List<com.mobile.micasaestucasa.domain.model.chat.Conversation>,
        currentUserId: String
    ) {
        viewModelScope.launch {
            val usersMap = mutableMapOf<String, User>()
            val propsMap = mutableMapOf<String, String>()
            for (conv in conversations) {
                val otherUserId = if (currentUserId == conv.hostId) conv.renterId else conv.hostId
                if (otherUserId !in usersMap) {
                    userRepo.getUserById(otherUserId)
                        .onSuccess { user ->
                            if (user != null) usersMap[otherUserId] = user
                        }
                }
                // fetch property title if not already cached
                if (conv.propertyId.isNotBlank() && conv.propertyId !in propsMap) {
                    getPropertyByIdUseCase(conv.propertyId)
                        .onSuccess { prop -> propsMap[conv.propertyId] = prop.title }
                }
            }
            _conversationUsers.value = usersMap
            _conversationProperties.value = propsMap
        }
    }

    fun reportUser(
        reporterId: String,
        reportedUserId: String,
        reason: String,
        description: String = "",
        propertyId: String? = null
    ) {
        viewModelScope.launch {
            addUserReportUseCase(
                reporterId = reporterId,
                reportedUserId = reportedUserId,
                reason = reason,
                description = description,
                propertyId = propertyId
            )
        }
    }

    fun sendMessageWithImage(
        uri: android.net.Uri,
        conversationId: String,
        senderId: String,
        text: String = ""
    ) {
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState.Uploading

            val uploadResult = storageRepository.uploadChatImage(
                uri = uri,
                conversationId = conversationId,
                senderId = senderId
            )

            if (uploadResult.isFailure) {
                _imageUploadState.value = ImageUploadState.Error(
                    uploadResult.exceptionOrNull()?.message ?: "Upload fallito"
                )
                return@launch
            }

            val imageUrl = uploadResult.getOrThrow()
            _imageUploadState.value = ImageUploadState.Success

            sendMessageUseCase(
                conversationId = conversationId,
                senderId = senderId,
                text = text,
                imageUrl = imageUrl
            )

            _imageUploadState.value = ImageUploadState.Idle
        }
    }
}

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Uploading : ImageUploadState()
    object Success : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
