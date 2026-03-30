package com.mobile.micasaestucasa.infra.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.User
import com.mobile.micasaestucasa.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getUser(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    override suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }
}
