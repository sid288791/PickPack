/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.di

import com.autopart.ordermgmt.data.repository.NotificationRepository
import com.autopart.ordermgmt.data.repository.NotificationRepositoryImpl
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Notification Module
 * Follows Dependency Inversion Principle - provides abstractions
 * Follows Interface Segregation Principle - focused on notification dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging {
            return FirebaseMessaging.getInstance()
        }
    }
}
