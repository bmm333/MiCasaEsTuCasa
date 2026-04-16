package com.mobile.micasaestucasa.data.mapper.user

import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole

fun UserDTO.toDomain(): User {
    return User(
        id = id ?: "",
        name = name ?: "",
        email = email ?: "",
        roles = roles
            ?.mapNotNull { UserRole.fromString(it) }
            ?: listOf(UserRole.GUEST)

    )
}
