package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class CreatePropertyUseCase @Inject constructor(
    private val propertyRepo: PropertyRepo,
    private val userRepo: UserRepo
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
        if (property.availableFrom.isBlank() || property.availableTo.isBlank()) {
            return Result.failure(IllegalArgumentException("Date di disponibilita obbligatorie"))
        }
        val fromDate = try {
            LocalDate.parse(property.availableFrom)
        } catch (_: DateTimeParseException) {
            return Result.failure(IllegalArgumentException("Data inizio non valida"))
        }
        val toDate = try {
            LocalDate.parse(property.availableTo)
        } catch (_: DateTimeParseException) {
            return Result.failure(IllegalArgumentException("Data fine non valida"))
        }
        if (fromDate.isAfter(toDate)) {
            return Result.failure(IllegalArgumentException("La data di inizio deve precedere la data di fine"))
        }
        if (toDate.isBefore(LocalDate.now())) {
            return Result.failure(IllegalArgumentException("La disponibilita non puo terminare nel passato"))
        }
        val result = propertyRepo.createProperty(property)

        if (result.isSuccess) {
            val currentUser = userRepo.getCurrentUser()
            if (currentUser != null && !currentUser.roles.contains(com.mobile.micasaestucasa.domain.model.user.UserRole.OWNER)) {
                val updatedRoles = currentUser.roles + com.mobile.micasaestucasa.domain.model.user.UserRole.OWNER
                userRepo.updateUserRolesAndBadge(currentUser.id, updatedRoles, com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_HOST)
            }
        }

        return result
    }
}
