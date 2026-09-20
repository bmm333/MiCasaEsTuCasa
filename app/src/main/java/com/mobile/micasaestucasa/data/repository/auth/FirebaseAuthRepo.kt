package com.mobile.micasaestucasa.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.model.user.UserStatus
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepo @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepo {
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

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Google sign in failed: user is null")

            // Check if user document already exists
            val docRef = firestore.collection("users").document(user.uid)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                // First time sign in, create a default profile
                val nameParts = user.displayName?.split(" ") ?: emptyList()
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                val userDto = UserDTO(
                    id = user.uid,
                    name = firstName,
                    lastName = lastName,
                    email = user.email ?: "",
                    roles = listOf(UserRole.GUEST.name),
                    createdAt = System.currentTimeMillis(),
                    status = UserStatus.ACTIVE.name,
                    profileCompleted = false
                )

                docRef.set(userDto, SetOptions.merge()).await()
            } else {
                // If the user already exists, but the user is banned or suspended
                val statusStr = snapshot.getString("status")
                if (statusStr == UserStatus.BANNED.name || statusStr == UserStatus.SUSPENDED.name) {
                    // Sign out to prevent banned/suspended users from keeping their session
                    firebaseAuth.signOut()
                    throw SecurityException("Account is $statusStr")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // re-authentication
            // se l'utente si è loggato molto tempo fa
            firebaseAuth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // utente deve rifare il login prima di eliminare l'account
            Result.failure(Exception("Per eliminare l'account devi effettuare nuovamente il login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
