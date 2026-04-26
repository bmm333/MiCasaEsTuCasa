package com.mobile.micasaestucasa.data.dto.review

data class ReviewDto(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val stars: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val propertyId: String = "",
    val authorId: String = ""
)
