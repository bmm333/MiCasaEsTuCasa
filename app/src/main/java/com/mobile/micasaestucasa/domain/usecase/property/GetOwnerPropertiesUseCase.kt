package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import javax.inject.Inject

class GetOwnerPropertiesUseCase @Inject constructor(private val propertyRepo: PropertyRepo) {
    suspend operator fun invoke(ownerId: String): Result<List<Property>> {
        if(ownerId.isBlank())
        {
            return Result.failure(IllegalArgumentException("Id Proprietario obbligatorio"))
        }
        return propertyRepo.getPropertiesByOwner(ownerId)
    }
}
