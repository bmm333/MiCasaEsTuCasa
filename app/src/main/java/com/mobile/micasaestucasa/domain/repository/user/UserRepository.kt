package com.mobile.micasaestucasa.domain.repository.user

import com.mobile.micasaestucasa.domain.model.user.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
}