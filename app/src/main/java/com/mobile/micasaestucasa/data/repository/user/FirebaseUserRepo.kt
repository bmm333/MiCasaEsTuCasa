package com.mobile.micasaestucasa.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.data.mapper.user.toDomain
import com.mobile.micasaestucasa.data.mapper.user.toDto
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserBadge
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                Result.failure(Exception("User not found in Firestore"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id)
                .set(user.toDto())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBadge(
        hostId: String,
        badge: UserBadge,
        avgRating: Double,
        reviewsCount: Int
    ): Result<Unit> {
        return try {
            usersCollection.document(hostId).update(
                mapOf(
                    "badge" to badge.name,
                    "avgRating" to avgRating,
                    "reviewsCount" to reviewsCount
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRenterScore(
        renterId: String,
        reliabilityScore: Double,
        renterReviewsCount: Int,
        badge: UserBadge
    ): Result<Unit> {
        return try {
            usersCollection.document(renterId).update(
                mapOf(
                    "badge" to badge.name,
                    "reliabilityScore" to reliabilityScore,
                    "renterReviewsCount" to renterReviewsCount
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getUserById(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            val dto = snapshot.toObject(UserDTO::class.java)
                ?: return Result.failure(IllegalStateException("UserDto is null"))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRolesAndBadge(userId: String, roles: List<com.mobile.micasaestucasa.domain.model.user.UserRole>, badge: com.mobile.micasaestucasa.domain.model.user.UserBadge?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "roles" to roles.map { it.name }
            )
            if (badge != null) {
                updates["badge"] = badge.name
            }
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Osserva in realtime lo stato di presenza di un utente.
     * Emette Pair(isOnline, lastSeen) ogni volta che Firestore notifica una modifica.
     */
    override fun observeUserOnlineStatus(uid: String): Flow<Pair<Boolean, Long?>> = callbackFlow {
        val docRef = usersCollection.document(uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(Pair(false, null))
                return@addSnapshotListener
            }
            val isOnline = snapshot.getBoolean("isOnline") ?: false
            val lastSeen = snapshot.getLong("lastSeen")
            trySend(Pair(isOnline, lastSeen))
        }
        awaitClose { listener.remove() }
    }

    /**
     * Aggiorna il campo isOnline e lastSeen dell'utente su Firestore.
     * Chiamare con isOnline=true al login/avvio app, false all'uscita.
     */
    override suspend fun updatePresence(uid: String, isOnline: Boolean) {
        try {
            val updates = mutableMapOf<String, Any>(
                "isOnline" to isOnline
            )
            if (!isOnline) {
                updates["lastSeen"] = System.currentTimeMillis()
            }
            usersCollection.document(uid).update(updates).await()
        } catch (_: Exception) {
            // ignoriamo eventuali errori di rete per la presenza
        }
    }
}
