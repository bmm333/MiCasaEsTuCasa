package com.mobile.micasaestucasa.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepo @Inject constructor(private val firebaseAuth: FirebaseAuth) : AuthRepo {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed: UID is null")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    /** Sends a password reset email via Firebase Authentication. */
    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
