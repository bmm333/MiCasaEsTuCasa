package com.mobile.micasaestucasa.data.mapper

import com.mobile.micasaestucasa.data.dto.UserDTO
import com.mobile.micasaestucasa.domain.model.User
import com.mobile.micasaestucasa.domain.model.UserRole

fun  UserDTO.toDomain(): User{
    return User(
        id = id ?: "",
        name = name ?: "",
        email = email ?: "",
        roles = roles
            ?.mapNotNull { UserRole.fromString(it) }
            ?: listOf(UserRole.GUEST)

    )
}