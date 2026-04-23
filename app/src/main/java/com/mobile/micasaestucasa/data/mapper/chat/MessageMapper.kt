package com.mobile.micasaestucasa.data.mapper.chat

import com.mobile.micasaestucasa.data.dto.chat.MessageDto
import com.mobile.micasaestucasa.domain.model.chat.Message


fun MessageDto.toDomain(): Message =Message(
    id = id ?: "",
    conversationId =conversationId ?: "",
    senderId= senderId ?: "",
    text = text ?: "",
    imageUrl = imageUrl,
    timestamp= timestamp ?: 0L,
    isRead = isRead ?: false
)
fun Message.toDto(): MessageDto = MessageDto(
    id= id,
    conversationId = conversationId,
    senderId = senderId,
    text = text,
    imageUrl = imageUrl,
    timestamp= timestamp,
    isRead = isRead
);