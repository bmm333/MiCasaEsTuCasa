package com.mobile.micasaestucasa.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mobile.micasaestucasa.data.repository.auth.FirebaseAuthRepo
import com.mobile.micasaestucasa.data.repository.booking.FirebaseBookingRepo
import com.mobile.micasaestucasa.data.repository.property.FirebasePropertyRepo
import com.mobile.micasaestucasa.data.repository.user.FirebaseUserRepo
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
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
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth
    ): UserRepo = FirebaseUserRepo(firebaseAuth)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepo {
        return FirebaseAuthRepo(firebaseAuth)
    }

    @Provides
    @Singleton
    fun providePropertyRepository(
        firestore: FirebaseFirestore
    ): PropertyRepo = FirebasePropertyRepo(firestore)

    @Provides
    @Singleton
    fun provideBookingRepository(
        firestore: FirebaseFirestore
    ): BookingRepo = FirebaseBookingRepo(firestore)

}
