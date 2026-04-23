package com.mobile.micasaestucasa.data.dto.chat

import com.google.firebase.Timestamp

data class MessageDto(
    val id: String?=null,
    val conversationId:String?=null,
    val senderId:String?=null,
    val text:String?=null,
    val imageUrl:String?=null,
    val timestamp: Long?=null,
    val isRead:Boolean?=null
);
