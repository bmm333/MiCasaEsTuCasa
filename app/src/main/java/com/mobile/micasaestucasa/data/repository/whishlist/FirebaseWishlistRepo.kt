package com.mobile.micasaestucasa.data.repository.wishlist

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseWishlistRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : WhishlistRepo {

    override suspend fun getSavedProperties(userId: String): Result<List<Property>> {
        return try {
            // Logica d'esempio: recupera i documenti dalla collezione 'wishlists' dell'utente
            val snapshot = firestore.collection("users").document(userId)
                .collection("saved_properties").get().await()
            // Qui andrebbe aggiunto il mapping come visto in FirebasePropertyRepo
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCollections(userId: String): Result<List<Collection>> {
        return try {
            // Logica per le collezioni
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}