package com.mobile.micasaestucasa.data.repository.property

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.data.mapper.property.toDomain
import com.mobile.micasaestucasa.data.mapper.property.toDto
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class FirebasePropertyRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : PropertyRepo {

    private val collection = firestore.collection("properties")

    override suspend fun createProperty(property: Property): Result<String> {
        return try {
            val docRef = collection.document()
            val dto = property.copy(id = docRef.id).toDto()
            docRef.set(dto).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProperty(property: Property): Result<String> {
        return try {
            collection.document(property.id).set(property.toDto()).await()
            Result.success(property.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProperty(id: String): Result<String> {
        return try {
            collection.document(id).delete().await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertiesByOwner(ownerId: String): Result<List<Property>> {
        return try {
            val snapshot = collection
                .whereEqualTo("ownerId", ownerId)
                .get().await()
            val properties = snapshot.documents
                .mapNotNull { it.toObject(PropertyDto::class.java)?.toDomain() }
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProperties(
        city: String,
        startDate: String,
        endDate: String,
        capacity: Int,
        keywords: List<String>
    ): Result<List<Property>> {
        return try {
            val query = collection
                .whereEqualTo("city", city)
                .whereGreaterThanOrEqualTo("capacity", capacity)
            val snapshot = query.get().await()
            var results = snapshot.documents
                .mapNotNull { it.toObject(PropertyDto::class.java)?.toDomain() }

            val requestedStartDate = LocalDate.parse(startDate)
            val requestedEndDate = LocalDate.parse(endDate)

            results = results.filter { property ->
                val availableFrom = parseDateOrNull(property.availableFrom)
                val availableTo = parseDateOrNull(property.availableTo)

                availableFrom != null &&
                    availableTo != null &&
                    !requestedStartDate.isBefore(availableFrom) &&
                    !requestedEndDate.isAfter(availableTo)
            }

            if (keywords.isNotEmpty()) {
                results = results.filter { property ->
                    keywords.any { kw -> property.keywords.contains(kw) }
                }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertyById(id: String): Result<Property> {
        return try {
            val doc = collection.document(id).get().await()
            val property = doc.toObject(PropertyDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Proprietà non trovata"))
            Result.success(property)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDateOrNull(value: String): LocalDate? {
        return try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            null
        }
    }
}
