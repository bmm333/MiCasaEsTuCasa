package com.mobile.micasaestucasa.domain.repository.auth

interface AuthRepo {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<String>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}
