package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import javax.inject.Inject

class DeletePropertyUseCase @Inject constructor(
    private val propertyRepo: PropertyRepo
) {
    suspend operator fun invoke(propertyId: String): Result<String> {
        if (propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Property id is required to delete"))
        }
        return propertyRepo.deleteProperty(propertyId)
    }
}
