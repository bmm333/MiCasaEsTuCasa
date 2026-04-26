package com.mobile.micasaestucasa.domain.model.review

data class Review(
    val id:String,
    val Title: String,
    val Body: String,
    val stars: Int=0,
    val createdAt: Long=System.currentTimeMillis(),
    val propertyId: String,
    val authorId: String
)
