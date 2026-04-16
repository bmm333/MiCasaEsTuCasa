package com.mobile.micasaestucasa.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepo @Inject constructor(private val firebaseAuth: FirebaseAuth) : AuthRepo {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            // chiamo firebase
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}
