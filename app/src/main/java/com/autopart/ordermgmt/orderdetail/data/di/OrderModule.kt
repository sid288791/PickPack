/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderdetail.data.di

import com.autopart.ordermgmt.orderdetail.data.repository.OrderRepositoryImpl
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for Order
 * Follows Clean Architecture - data layer provides implementation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OrderModule {
    
    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
}
