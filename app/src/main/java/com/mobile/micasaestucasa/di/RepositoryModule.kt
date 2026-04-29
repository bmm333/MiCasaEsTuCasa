package com.mobile.micasaestucasa.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.mobile.micasaestucasa.data.repository.auth.FirebaseAuthRepo
import com.mobile.micasaestucasa.data.repository.booking.FirebaseBookingRepo
import com.mobile.micasaestucasa.data.repository.chat.FirebaseChatRepo
import com.mobile.micasaestucasa.data.repository.notification.FirebaseNotificationRepo
import com.mobile.micasaestucasa.data.repository.property.FirebasePropertyRepo
import com.mobile.micasaestucasa.data.repository.user.FirebaseUserRepo
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
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
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepo = FirebaseUserRepo(firebaseAuth, firestore)

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

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepo = FirebaseChatRepo(firestore)

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        messaging: FirebaseMessaging
    ): NotificationRepo = FirebaseNotificationRepo(
        firestore,
        messaging
    )
}
