package com.mobile.micasaestucasa.domain.repository.auth

interface AuthRepo {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout()
}
