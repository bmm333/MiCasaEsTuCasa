package com.mobile.micasaestucasa.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<String>
)
