/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autopart.ordermgmt.domain.usecase.GetFCMTokenUseCase
import com.autopart.ordermgmt.domain.usecase.SubscribeToTopicUseCase
import com.autopart.ordermgmt.domain.usecase.UnsubscribeFromTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Notification ViewModel
 * Follows Single Responsibility Principle - manages notification UI state
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val subscribeToTopicUseCase: SubscribeToTopicUseCase,
    private val unsubscribeFromTopicUseCase: UnsubscribeFromTopicUseCase
) : ViewModel() {

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeFCMToken()
    }

    private fun observeFCMToken() {
        viewModelScope.launch {
            getFCMTokenUseCase().collect { token ->
                _fcmToken.value = token
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                subscribeToTopicUseCase(topic)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to subscribe to topic"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                unsubscribeFromTopicUseCase(topic)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to unsubscribe from topic"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
