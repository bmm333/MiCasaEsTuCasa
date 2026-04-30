package com.mobile.micasaestucasa.data.dto.user

data class UserDTO(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val roles: List<String>? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val address: String? = null,
    val phone: String? = null
)
