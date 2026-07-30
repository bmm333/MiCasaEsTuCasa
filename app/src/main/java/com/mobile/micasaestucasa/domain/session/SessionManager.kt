package com.mobile.micasaestucasa.domain.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mobile.micasaestucasa.domain.model.user.UserStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors the current user's account status in real time.
 * Emits UserStatus.BANNED or UserStatus.SUSPENDED when an admin action takes effect.
 */
@Singleton
class SessionManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    val sessionStatus: Flow<UserStatus?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        fun startUserListener(uid: String) {
            firestoreListener?.remove()
            firestoreListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val statusStr = snapshot?.getString("status") ?: "ACTIVE"
                    val status = try {
                        UserStatus.valueOf(statusStr)
                    } catch (_: Exception) {
                        UserStatus.ACTIVE
                    }
                    trySend(status)
                }
        }

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                firestoreListener?.remove()
                firestoreListener = null
                trySend(null)
            } else {
                startUserListener(uid)
            }
        }

        firebaseAuth.addAuthStateListener(authListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }
}
