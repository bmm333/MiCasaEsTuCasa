package com.mobile.micasaestucasa.domain.repository.user

import com.mobile.micasaestucasa.domain.model.user.User

interface UserRepo {
    suspend fun getCurrentUser(): User?
}
