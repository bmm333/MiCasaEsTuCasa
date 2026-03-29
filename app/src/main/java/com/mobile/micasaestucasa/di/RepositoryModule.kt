package com.mobile.micasaestucasa.di

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.data.repository.auth.FirebaseAuthRepo
import com.mobile.micasaestucasa.data.repository.user.FirebaseUserRepo
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth= FirebaseAuth.getInstance()
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth
    ): UserRepository= FirebaseUserRepo(firebaseAuth)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepo{
        return FirebaseAuthRepo(firebaseAuth)
    }
}