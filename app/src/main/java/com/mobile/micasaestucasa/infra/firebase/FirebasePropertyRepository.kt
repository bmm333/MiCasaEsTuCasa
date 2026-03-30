package com.mobile.micasaestucasa.infra.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.Property
import com.mobile.micasaestucasa.domain.repository.PropertyRepository
import kotlinx.coroutines.tasks.await

class FirebasePropertyRepository(
    private val firestore: FirebaseFirestore
) : PropertyRepository {

    private val propertiesCollection = firestore.collection("properties")

    override suspend fun getProperty(propertyId: String): Property? {
        return try {
            propertiesCollection.document(propertyId).get().await().toObject(Property::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllProperties(): List<Property> {
        return try {
            propertiesCollection.get().await().toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveProperty(property: Property) {
        val docRef = if (property.id.isEmpty()) {
            propertiesCollection.document()
        } else {
            propertiesCollection.document(property.id)
        }
        
        val propertyToSave = if (property.id.isEmpty()) {
            property.copy(id = docRef.id)
        } else {
            property
        }
        
        docRef.set(propertyToSave).await()
    }

    override suspend fun deleteProperty(propertyId: String) {
        propertiesCollection.document(propertyId).delete().await()
    }
}
