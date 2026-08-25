package com.autopart.ordermgmt.orderdetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.usecase.AdvanceOrderUseCase
import com.autopart.ordermgmt.orderdetail.domain.usecase.GetOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Order Detail Screen
 * Manages order items and their expansion state
 * Follows Clean Architecture: ViewModel -> UseCase -> Repository -> Data Layer
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderUseCase: GetOrderUseCase,
    private val advanceOrderUseCase: AdvanceOrderUseCase
) : ViewModel() {

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentAction = MutableStateFlow("ALLOCATED")
    val currentAction: StateFlow<String> = _currentAction.asStateFlow()

    private val _isAdvancing = MutableStateFlow(false)
    val isAdvancing: StateFlow<Boolean> = _isAdvancing.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val employeeId: String = generateEmployeeId()

    fun setOrder(orderData: Order) {
        _order.value = orderData
        _currentAction.value = orderData.orderStatus
    }

    fun loadOrder(orderId: String?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val orderData = getOrderUseCase(orderId)
                _order.value = orderData
                orderData?.let { _currentAction.value = it.orderStatus }
            } catch (e: Exception) {
                _order.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun advanceOrder(taskId: String) {
        if (_isCompleted.value || _isAdvancing.value) return

        viewModelScope.launch {
            try {
                _isAdvancing.value = true
                val response = advanceOrderUseCase(taskId, employeeId)
                response?.let {
                    val nextAction = it.nextAction
                    if (nextAction.isNullOrBlank()) {
                        _isCompleted.value = true
                        _currentAction.value = "COMPLETED"
                    } else {
                        _currentAction.value = nextAction
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isAdvancing.value = false
            }
        }
    }

    private fun generateEmployeeId(): String {
        return (10000..99999).random().toString()
    }

}
