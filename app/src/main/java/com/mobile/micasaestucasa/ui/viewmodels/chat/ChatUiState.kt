package com.mobile.micasaestucasa.ui.viewmodels.chat

import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.model.chat.Message

sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()
    data class ConversationsLoaded(val conversations: List<Conversation>) : ChatUiState()
    data class ConversationReady(val conversation: Conversation) : ChatUiState()
    object MessageSent : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}