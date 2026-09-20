package com.mobile.micasaestucasa.data.repository.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository per l'upload di file su Firebase Storage.
 *
 * Path convention:
 * - Foto chat:    `chat/{conversationId}/{timestamp}_{senderId}.jpg`
 * - Foto profilo: `profile_photos/{userId}.jpg`
 *
 * Ogni upload ritorna l'URL pubblico del file — salvato come
 * campo `imageUrl` nel documento messaggio su Firestore.
 */
class FirebaseStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * Carica un'immagine chat su Firebase Storage.
     *
     * @param uri URI locale dell'immagine selezionata dal picker.
     * @param conversationId ID della conversazione — usato come cartella.
     * @param senderId UID del mittente — usato nel nome del file.
     * @return [Result] con l'URL pubblico del file caricato.
     */
    suspend fun uploadChatImage(
        uri: Uri,
        conversationId: String,
        senderId: String
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val ref = storage.reference
                .child("chat/$conversationId/${timestamp}_$senderId.jpg")

            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val downloadUrl = kotlinx.coroutines.suspendCancellableCoroutine<String> { cont ->
                ref.putFile(uri, metadata)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception ?: Exception("Upload fallito")
                        }
                        ref.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        cont.resume(downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        cont.resumeWithException(exception)
                    }
            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Carica una foto profilo su Firebase Storage.
     * Sovrascrive la foto precedente — stesso path per ogni utente.
     *
     * @param uri URI locale dell'immagine.
     * @param userId UID dell'utente.
     * @return [Result] con l'URL pubblico.
     */
    suspend fun uploadProfilePhoto(uri: Uri, userId: String): Result<String> {
        return try {
            val ref = storage.reference
                .child("profile_photos/$userId.jpg")

            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val downloadUrl = kotlinx.coroutines.suspendCancellableCoroutine<String> { cont ->
                ref.putFile(uri, metadata)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception ?: Exception("Upload fallito")
                        }
                        ref.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        cont.resume(downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        cont.resumeWithException(exception)
                    }
            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
