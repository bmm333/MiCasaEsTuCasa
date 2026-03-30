package com.mobile.micasaestucasa.domain.repository

import com.mobile.micasaestucasa.domain.model.User

interface UserRepository {
    suspend fun getUser(userId: String): User?
    suspend fun saveUser(user: User)
    suspend fun deleteUser(userId: String)
}
