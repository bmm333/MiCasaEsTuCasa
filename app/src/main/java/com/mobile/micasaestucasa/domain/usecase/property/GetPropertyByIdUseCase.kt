package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import javax.inject.Inject

class GetPropertyByIdUseCase @Inject constructor(private val propertyRepo: PropertyRepo) {
    suspend operator fun invoke(propertyId: String): Result<Property> {
        if (propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID proprietà obbligatorio"))
        }
        return propertyRepo.getPropertyById(propertyId)
    }
}
