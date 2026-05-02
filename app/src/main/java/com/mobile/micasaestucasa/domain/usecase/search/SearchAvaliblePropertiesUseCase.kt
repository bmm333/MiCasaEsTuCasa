package com.mobile.micasaestucasa.domain.usecase.search

import com.mobile.micasaestucasa.domain.model.search.SearchQuery
import com.mobile.micasaestucasa.domain.model.search.SearchResult
import com.mobile.micasaestucasa.domain.model.search.SearchSortOrder
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Use case for the search of avalible properties in the selected dates.
 *
 * Diff to `SearchPropertiesUseCase` which filters only by citty and capacity this adds the
 * avaliblity filter excludign the properties which have an active booking in the requested dates of the query
 *
 * @property propertyRepo Property Repository
 * @property bookingRepo Booking repository used to check for avaliblity
 * */
class SearchAvaliblePropertiesUseCase @Inject constructor(
    private val propertyRepo: PropertyRepo,
    private val bookingRepo: BookingRepo
) {
    /**
     * Executes the query with avalibility
     * @param query `SearchQuery` with all the params of the search
     * @return `Result` with list of `SearchResult` filtered and ordered
     * */
    suspend operator fun invoke(query: SearchQuery): Result<List<SearchResult>> {
        if (query.city.isBlank()) {
            return Result.failure(IllegalArgumentException("City is required"))
        }
        if (query.guestsCount <= 0) {
            return Result.failure(IllegalArgumentException("Guest count must be at least 1"))
        }
        if (query.startDate >= query.endDate) {
            return Result.failure(IllegalArgumentException("Start date must be before end date"))
        }
        val nights = calculateNights(query.startDate, query.endDate)
        if (nights <= 0) {
            return Result.failure(IllegalArgumentException("Stay should be at least 1 night"))
        }
        val propertiesResult = propertyRepo.searchProperties(
            city = query.city,
            startDate = query.startDate,
            endDate = query.endDate,
            capacity = query.guestsCount,
            keywords = query.keywords
        )
        if (propertiesResult.isFailure) {
            return Result.failure(propertiesResult.exceptionOrNull()!!)
        }
        val properties = propertiesResult.getOrThrow()
        // we need to check avaliblity for each property of the result , therefore we need to paralellize this operation to not block using a corutinescope
        val results = properties.mapNotNull { property ->
            val ovelapResult = bookingRepo.hasOverlappingBooking(
                propertyId = property.id,
                startDate = query.startDate,
                endDate = query.endDate
            )
            val isAvalible = ovelapResult.getOrDefault(true).not()
            // price filetr
            if (query.maxPricePerDay != null && property.pricePerDay > query.maxPricePerDay) {
                return@mapNotNull null
            }
            SearchResult(
                property = property,
                totalPrice = property.pricePerDay * nights,
                nights = nights,
                isAvalible = isAvalible
            )
        }
        val available = results
            .filter { it.isAvalible }
            .let { list -> sortResults(list, query.sortOrder) }

        return Result.success(available)
    }
    private fun sortResults(
        results: List<SearchResult>,
        order: SearchSortOrder
    ): List<SearchResult> = when (order) {
        SearchSortOrder.RELEVANCE -> results.sortedByDescending {
            it.property.rating * 0.7 + (1.0 / (it.property.pricePerDay + 1)) * 0.3
        }
        SearchSortOrder.PRICE_ASC -> results.sortedBy { it.property.pricePerDay }
        SearchSortOrder.PRICE_DESC -> results.sortedByDescending { it.property.pricePerDay }
        SearchSortOrder.RATING_DESC -> results.sortedByDescending { it.property.rating }
    }
    private fun calculateNights(startDate: String, endDate: String): Int {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            ChronoUnit.DAYS.between(start, end).toInt()
        } catch (e: Exception) {
            -1
        }
    }
}
