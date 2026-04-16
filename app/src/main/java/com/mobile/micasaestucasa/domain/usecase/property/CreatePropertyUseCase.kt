package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import javax.inject.Inject

class CreatePropertyUseCase @Inject constructor(
    private val propertyRepo: PropertyRepo
) {
    suspend operator fun invoke(property: Property): Result<String> {
        if (property.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Titolo obbligatorio"))
        }
        if (property.pricePerDay <= 0) {
            return Result.failure(IllegalArgumentException("Prezzo deve essere maggiore di 0"))
        }
        if (property.capacity <= 0) {
            return Result.failure(IllegalArgumentException("Capacita deve essere maggiore di 0"))
        }
        if (property.city.isBlank()) {
            return Result.failure(IllegalArgumentException("Citta obbligatoria"))
        }
        return propertyRepo.createProperty(property)
    }
}
