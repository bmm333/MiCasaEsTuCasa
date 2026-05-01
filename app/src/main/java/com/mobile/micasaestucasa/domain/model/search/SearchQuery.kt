package com.mobile.micasaestucasa.domain.model.search


/**
 * Encapsulates all the parameters used to search for properties
 * respecting the OCP.
 *
 * @property city The city to search for properties in.
 * @property startDate The start date of the search.
 * @property endDate The end date of the search.
 * @property guestsCount The number of guests for the search.
 * @property keywords A list of keywords to search for in the property descriptions and filter amenities
 * @property maxPricePerDay The maximum price per day for the search, Null=no limit
 * @property sortOrder The order in which to sort the results.
 * */
data class SearchQuery(
    val city: String,
    val startDate: String,
    val endDate: String,
    val guestsCount: Int,
    val keywords: List<String> = emptyList(),
    val maxPricePerDay: Double? = null,
    val sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE
)

enum class SearchSortOrder {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    RATING_DESC
}