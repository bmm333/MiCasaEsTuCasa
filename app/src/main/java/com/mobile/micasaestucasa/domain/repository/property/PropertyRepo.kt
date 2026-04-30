package com.mobile.micasaestucasa.domain.repository.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface PropertyRepo {
    suspend fun createProperty(property: Property): Result<String>
    suspend fun updateProperty(property: Property): Result<String>
    suspend fun deleteProperty(id: String): Result<String>
    suspend fun getPropertiesByOwner(ownerId: String): Result<List<Property>>
    suspend fun searchProperties(
        city: String,
        startDate: String,
        endDate: String,
        capacity: Int,
        keywords: List<String> = emptyList()
    ): Result<List<Property>>
    suspend fun getPropertyById(id: String): Result<Property>

    // Supporto Real-time per la Home
    fun getAllPropertiesFlow(): Flow<Resource<List<Property>>>
    suspend fun getCategories(): Result<List<com.mobile.micasaestucasa.ui.viewmodels.home.Category>>
}
