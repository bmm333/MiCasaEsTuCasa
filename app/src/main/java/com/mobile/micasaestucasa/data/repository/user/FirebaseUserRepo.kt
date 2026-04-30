package com.mobile.micasaestucasa.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.data.mapper.user.toDomain
import com.mobile.micasaestucasa.data.mapper.user.toDto
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepo @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepo {

    private val usersCollection = firestore.collection("users")

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return getUserProfile(firebaseUser.uid).getOrNull()
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val dto = document.toObject(UserDTO::class.java)
            if (dto != null) {
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception("Profilo utente non trovato in Firestore"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
