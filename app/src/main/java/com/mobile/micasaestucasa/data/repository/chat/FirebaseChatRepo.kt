package com.mobile.micasaestucasa.data.repository.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobile.micasaestucasa.data.mapper.chat.toDto
import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firestore implementation of ChatRepo
 *
 * using the following structure:
 * -    conversations/{conversationId}
 * -    messages/{messageId}
 *
 * message->subcollection of conversations (gives less complex sec rules && easier queries)
 * using calbackFlow adapter (firebase-> callback(push),Kotlin->flow )
 *
 * @property firestore instance of FirebaseFirestore injected by hilt
 * */
class FirebaseChatRepo @Inject constructor(private val firestore: FirebaseFirestore): ChatRepo {
    private val conversationsCollection=firestore.collection("conversations")
    /**
     * Creates the message doc and updates atomically lastmsg
     * of the conversation using batch wr
     * batch wr -> both wr ok or nothing.
     */
    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        imageUrl: String?
    ): Result<Message> {
        return try{
            val messageRef=conversationsCollection
                .document(conversationId)
                .collection("messages")
                .document()
            val message=Message(
                id  =messageRef.id,
                conversationId = conversationId,
                senderId= senderId,
                text= text,
                imageUrl= imageUrl,
                timestamp= System.currentTimeMillis(),
                isRead=false
            )
            //batch msg + update preview conv
            val batch=firestore.batch()
            batch.set(messageRef,message.toDto())
            batch.update(
                conversationsCollection.document(conversationId),
                mapOf(
                    "lastMessage" to text.ifBlank {"Image"},
                    "lastMessageTimestamp" to message.timestamp
                )
            )
            batch.commit().await()
            Result.success(message)
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }


    //missing osberveMessages i still have to go through docs https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html
    //and is missing on the chatrepo as well
    /**
     * Query to find existing conversation before creating a new one.
     * query based on three indexed fields (hostId,renterId,propertyId)
     */
    override suspend fun getOrCreateConversation(
        hostId: String,
        renterId: String,
        propertyId: String
    ): Result<Conversation> {
        return try{
            val existing=conversationsCollection
                .whereEqualTo("hostId",hostId)
                .whereEqualTo("renterId",renterId)
                .whereEqualTo("propertyId",propertyId)
                .limit(1) //as per business logic rules only one conv can exist
                .get().await()
            if(!existing.isEmpty)
            {
                val conversations=existing.documents.first().toObject(ConversationDto::class.java)?.toDomain()?:return Result.failure(Exception("Error deserializzazione conversatzione"))
                return Result.success(conversations)
            }
            val docRef=conversationsCollection.document()
            val conversation= Conversation(
                id=docRef.id,
                hostId=hostId,
                renterId=renterId,
                propertyId=propertyId,
                lastMessage = "",
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 0
            )
            docRef.set(conversation.toDto()).await()
            Result.success(conversation)
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }


    override suspend fun markMessagesAsRead(
        conversationId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val unread = conversationsCollection
                .document(conversationId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", userId)
                .get().await()
            //respecting firestore limit
            unread.documents.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    override suspend fun getConversationsForUser(userId: String): Result<List<Conversation>> {
        //no or on multiple fields in same query so two queries and merge
        val asHost=conversationsCollection
            .whereEqualTo("hostId",userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(ConversationDto::class.java)?.toDomain() }
        val asRenter=conversationsCollection
            .whereEqualTo("renterId",userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull {
                it.toObject(ConversationDto::class.java)?.toDomain()
            }
        //merge dedup and resorting
        val merged=(asHost+asRenter)
            .distinctBy { it.id }
            .sortedByDescending { it.lastMessageTimestamp }
        return Result.success(merged)
        }catch(e:Exception)
        {
            Result.failure(e)
        }
    }

}