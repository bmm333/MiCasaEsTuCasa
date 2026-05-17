package com.mobile.micasaestucasa.ui.viewmodels.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import com.mobile.micasaestucasa.domain.usecase.chat.GetOrCreateConversationUseCase
import com.mobile.micasaestucasa.domain.usecase.chat.SendMessageUseCase
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
    private val chatRepo: ChatRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _activeConversationId = MutableStateFlow("")
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

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
                .onSuccess { _uiState.value = ChatUiState.ConversationsLoaded(it) }
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
}
