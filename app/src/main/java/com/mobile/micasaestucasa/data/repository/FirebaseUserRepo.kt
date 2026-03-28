package com.mobile.micasaestucasa.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.data.dto.UserDTO
import com.mobile.micasaestucasa.data.mapper.toDomain
import com.mobile.micasaestucasa.domain.model.User
import com.mobile.micasaestucasa.domain.repository.UserRepository

class FirebaseUserRepo(private val firebaseAuth: FirebaseAuth) : UserRepository {
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