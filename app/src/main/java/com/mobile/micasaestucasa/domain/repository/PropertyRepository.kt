package com.mobile.micasaestucasa.domain.repository

import com.mobile.micasaestucasa.domain.model.Property

interface PropertyRepository {
    suspend fun getProperty(propertyId: String): Property?
    suspend fun getAllProperties(): List<Property>
    suspend fun saveProperty(property: Property)
    suspend fun deleteProperty(propertyId: String)

}
