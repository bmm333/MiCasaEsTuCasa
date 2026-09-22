package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class SearchPropertiesUseCase @Inject constructor(private val propertyRepo: PropertyRepo) {
    suspend operator fun invoke(
        city: String,
        startDate: String,
        endDate: String,
        capacity: Int,
        keywords: List<String> = emptyList()
    ): Result<List<Property>> {
        if (city.isBlank()) {
            return Result.failure(IllegalArgumentException("Citta Obbligatoria"))
        }
        if (capacity <= 0) {
            return Result.failure(IllegalArgumentException("Capacita non valdia"))
        }

        val requestedStartDate = parseDateOrNull(startDate)
            ?: return Result.failure(IllegalArgumentException("Start date non valida"))
        val requestedEndDate = parseDateOrNull(endDate)
            ?: return Result.failure(IllegalArgumentException("End date non valida"))

        if (requestedStartDate.isAfter(requestedEndDate)) {
            return Result.failure(IllegalArgumentException("Start date deve essere precedente o uguale a end date"))
        }

        return propertyRepo.searchProperties(city, startDate, endDate, capacity, keywords)
    }

    private fun parseDateOrNull(value: String): LocalDate? {
        if (value.isBlank()) return null

        return try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
