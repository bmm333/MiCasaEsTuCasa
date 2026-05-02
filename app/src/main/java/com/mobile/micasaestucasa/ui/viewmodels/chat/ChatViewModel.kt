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

    /**
     * sends a textual message
     * @param conversationId Id of the conversation
     * @param senderId UID of the sender
     * @param text Text of the message
     * */
    fun sendMessage(conversationId: String, senderId: String, text: String) {
        viewModelScope.launch {
            sendMessageUseCase(conversationId, senderId, text)
                .onSuccess { _uiState.value = ChatUiState.MessageSent }
                .onFailure {
                    _uiState.value = ChatUiState.Error(
                        it.message ?: "Errore sending message"
                    )
                }
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

    fun resetState() { _uiState.value = ChatUiState.Idle }
}
