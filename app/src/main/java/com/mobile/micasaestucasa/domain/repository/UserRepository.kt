package com.mobile.micasaestucasa.domain.repository

import com.mobile.micasaestucasa.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
}