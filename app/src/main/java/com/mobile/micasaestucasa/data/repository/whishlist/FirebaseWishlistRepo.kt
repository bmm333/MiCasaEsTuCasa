package com.mobile.micasaestucasa.data.repository.wishlist

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.data.mapper.property.toDomain
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseWishlistRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : WhishlistRepo {

    private fun savedCol(userId: String) =
        firestore.collection("users").document(userId).collection("saved_properties")

    override suspend fun getSavedProperties(userId: String): Result<List<Property>> {
        return try {
            val savedDocs = savedCol(userId).get().await()
            val propertyIds = savedDocs.documents.mapNotNull { it.getString("propertyId") }
            if (propertyIds.isEmpty()) return Result.success(emptyList())
            //Firestore whereIn max 30 ids per query
            val properties = mutableListOf<Property>()
            propertyIds.chunked(30).forEach { chunk ->
                val snapshot = firestore.collection("properties")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                    .get().await()
                snapshot.documents.forEach { doc ->
                    doc.toObject(PropertyDto::class.java)?.let { dto ->
                        properties.add(dto.toDomain())
                    }
                }
            }
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCollections(userId: String): Result<List<Collection>> {
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleSavedProperty(userId: String, propertyId: String): Result<Boolean> {
        return try {
            val docRef = savedCol(userId).document(propertyId)
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.delete().await()
                Result.success(false) // removed
            } else {
                docRef.set(mapOf("propertyId" to propertyId, "savedAt" to System.currentTimeMillis())).await()
                Result.success(true) // saved
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isPropertySaved(userId: String, propertyId: String): Result<Boolean> {
        return try {
            val doc = savedCol(userId).document(propertyId).get().await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Returns just the IDs of saved properties (lightweight) */
    override suspend fun getSavedPropertyIds(userId: String): Result<Set<String>> {
        return try {
            val snapshot = savedCol(userId).get().await()
            Result.success(snapshot.documents.mapNotNull { it.getString("propertyId") }.toSet())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
