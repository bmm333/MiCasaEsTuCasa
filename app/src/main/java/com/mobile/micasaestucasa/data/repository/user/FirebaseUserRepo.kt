package com.mobile.micasaestucasa.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.data.mapper.user.toDomain
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo

class FirebaseUserRepo(private val firebaseAuth: FirebaseAuth) : UserRepo {
    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { firebaseUser ->
            val dto = UserDTO(
                id = firebaseUser.uid,
                name = firebaseUser.displayName,
                email = firebaseUser.email,
                roles = listOf("GUEST")
            )
            dto.toDomain()
        }
    }
}
