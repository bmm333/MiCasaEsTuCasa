package com.mobile.micasaestucasa.data.repository.property

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.data.mapper.property.toDomain
import com.mobile.micasaestucasa.data.mapper.property.toDto
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.viewmodels.home.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class FirebasePropertyRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : PropertyRepo {

    private val propertiesCollection = firestore.collection("properties")
    private val categoriesCollection = firestore.collection("categories")

    override suspend fun createProperty(property: Property): Result<String> {
        return try {
            val docRef = propertiesCollection.document()
            val dto = property.copy(id = docRef.id).toDto()
            docRef.set(dto).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProperty(property: Property): Result<String> {
        return try {
            propertiesCollection.document(property.id).set(property.toDto()).await()
            Result.success(property.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProperty(id: String): Result<String> {
        return try {
            propertiesCollection.document(id).delete().await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertiesByOwner(ownerId: String): Result<List<Property>> {
        return try {
            val snapshot = propertiesCollection
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
            val query = propertiesCollection
                .whereEqualTo("city", city)
                .whereGreaterThanOrEqualTo("capacity", capacity)

            val snapshot = query.get().await()
            var results = snapshot.documents
                .mapNotNull { it.toObject(PropertyDto::class.java)?.toDomain() }

            if (startDate.isNotBlank() && endDate.isNotBlank()) {
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
            }

            if (keywords.isNotEmpty()) {
                results = results.filter { property ->
                    keywords.all { kw -> property.keywords.any { it.equals(kw, ignoreCase = true) } }
                }
            }

            results = results.filter { !it.isOnHold }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertyById(id: String): Result<Property> {
        return try {
            val doc = propertiesCollection.document(id).get().await()
            val property = doc.toObject(PropertyDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Proprietà non trovata"))
            if (property.isOnHold) {
                return Result.failure(Exception("Proprietà non disponibile"))
            }
            Result.success(property)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllPropertiesFlow(): Flow<Resource<List<Property>>> = callbackFlow {
        trySend(Resource.Loading)
        val subscription = propertiesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Errore Firestore", error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val properties = snapshot.documents
                    .mapNotNull { it.toObject(PropertyDto::class.java)?.toDomain() }
                    .filter { !it.isOnHold }
                trySend(Resource.Success(properties))
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val snapshot = categoriesCollection.get().await()
            val categories = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val icon = doc.getString("icon") ?: "home"
                Category(name, icon)
            }
            Result.success(categories)
        } catch (e: Exception) {
            // Se la collezione non esiste, restituisco default
            Result.success(
                listOf(
                    Category("Modern", "holiday_village"),
                    Category("Rustic", "cabin"),
                    Category("Beachfront", "beach_access"),
                    Category("Historic", "castle"),
                    Category("Urban", "apartment")
                )
            )
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
