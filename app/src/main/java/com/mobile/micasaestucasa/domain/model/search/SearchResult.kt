package com.mobile.micasaestucasa.domain.model.search

import com.mobile.micasaestucasa.domain.model.property.Property


/**
 * Represents the result of a search query.
 *
 * @property property The property that was searched for.
 * @property totalPrice The total price of the search.
 * @property nights The number of nights in the search.
 * @property isAvalible Whether the property is available for the search.
 * */
data class SearchResult(
    val property: Property,
    val totalPrice:Double,
    val nights:Int,
    val isAvalible: Boolean
)
