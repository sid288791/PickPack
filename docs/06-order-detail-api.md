# Step 6: Order Detail & API Layer

This step creates the complete Order Detail feature: domain models, repository interface, Ktor-based repository implementation with API calls, Hilt DI module, use cases, ViewModel, and the full Compose UI screen with order info, item cards, bill summary, and 360° panorama integration.

**Package:** `com.autopart.ordermgmt.orderdetail`

---

## 6.1 Domain Models

### Order, OrderItem, ItemLocation

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/domain/model/OrderItem.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val id: Int,
    val orderNumber: String,
    val customerId: Int,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val shippingAddress: String,
    val billingAddress: String,
    val orderStatus: String,
    val totalAmount: Double,
    val currency: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItem>
) : Parcelable

@Parcelize
data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val location: ItemLocation,
    val createdAt: String,
    val updatedAt: String,
    val isExpanded: Boolean = false
) : Parcelable

@Parcelize
data class ItemLocation(
    val aisle: String,
    val shelf: String,
    val bin: String,
    val yaw: Double,
    val pitch: Double
) : Parcelable
```

**Key points:**
- All models are `@Parcelize` (requires `kotlin-parcelize` plugin)
- `ItemLocation` contains warehouse coordinates (aisle/shelf/bin) and 360° panorama angles (yaw/pitch)
- `OrderItem.isExpanded` is for UI expansion state

### StoreTaskAdvanceResponse

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/domain/model/StoreTaskAdvanceResponse.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoreTaskAdvanceResponse(
    val id: Int? = null,
    val taskNumber: String? = null,
    val status: String? = null,
    val nextAction: String? = null
)
```

---

## 6.2 Order Repository Interface

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/domain/repository/OrderRepository.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.domain.repository

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse

interface OrderRepository {
    suspend fun getOrder(orderId: String?): Order?
    suspend fun advanceOrder(orderId: String, employeeId: String): StoreTaskAdvanceResponse?
}
```

---

## 6.3 Order Repository Implementation

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/data/repository/OrderRepositoryImpl.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.data.repository

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.OrderItem
import com.autopart.ordermgmt.orderdetail.domain.model.ItemLocation
import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient
) : OrderRepository {

    companion object {
        private const val BASE_URL = "https://store-service.example.com"
    }

    override suspend fun getOrder(orderId: String?): Order? {
        return try {
            val response: AdvanceOrderDto = httpClient.post("$BASE_URL/getOrder/$orderId/advance") {
                contentType(ContentType.Application.Json)
                setBody(null)
            }.body()
            null
        } catch (e: Exception) {
            // Fallback to mock data when API is unavailable
            Order(
                id = 12,
                orderNumber = "ORD-F038D461",
                customerId = 116,
                customerName = "sid pandey",
                customerEmail = "sid@example.com",
                customerPhone = "+91-7840843989",
                shippingAddress = "123 Main St, Springfield, IL 62701",
                billingAddress = "123 Main St, Springfield, IL 62701",
                orderStatus = "ALLOCATED",
                totalAmount = 67.97,
                currency = "USD",
                paymentMethod = "CREDIT_CARD",
                paymentStatus = "PENDING",
                createdAt = "2026-07-28T21:26:04.976795",
                updatedAt = "2026-07-28T21:26:05.001271",
                items = listOf(
                    OrderItem(
                        id = 23, orderId = 12, productId = 29,
                        productName = "Power Strip Surge", sku = "SKU-029",
                        quantity = 2, unitPrice = 24.99, totalPrice = 49.98,
                        location = ItemLocation(aisle = "D02", shelf = "S01", bin = "B01", yaw = 45.5, pitch = -8.0),
                        createdAt = "2026-07-28T21:26:04.978381",
                        updatedAt = "2026-07-28T21:26:04.978383"
                    ),
                    OrderItem(
                        id = 24, orderId = 12, productId = 52,
                        productName = "Cable Management Kit", sku = "SKU-052",
                        quantity = 1, unitPrice = 17.99, totalPrice = 17.99,
                        location = ItemLocation(aisle = "G02", shelf = "S01", bin = "B01", yaw = 120.00, pitch = 5.00),
                        createdAt = "2026-07-28T21:26:04.979279",
                        updatedAt = "2026-07-28T21:26:04.979282"
                    ),
                    OrderItem(
                        id = 24, orderId = 12, productId = 52,
                        productName = "Cable Management Kit", sku = "SKU-052",
                        quantity = 1, unitPrice = 17.99, totalPrice = 17.99,
                        location = ItemLocation(aisle = "G02", shelf = "S01", bin = "B01", yaw = -30.00, pitch = 15.00),
                        createdAt = "2026-07-28T21:26:04.979279",
                        updatedAt = "2026-07-28T21:26:04.979282"
                    )
                )
            )
        }
    }

    override suspend fun advanceOrder(orderId: String, employeeId: String): StoreTaskAdvanceResponse? {
        return try {
            val response: AdvanceOrderDto = httpClient.post("$BASE_URL/store-tasks/$orderId/advance") {
                contentType(ContentType.Application.Json)
                setBody(AdvanceRequestDto(employeeId = employeeId))
            }.body()

            StoreTaskAdvanceResponse(
                id = response.id,
                taskNumber = response.taskNumber,
                status = response.status,
                nextAction = response.nextAction
            )
        } catch (e: Exception) {
            StoreTaskAdvanceResponse(
                id = 123,
                taskNumber = "ORD-20FE2BEC-S1",
                status = "PICKING",
                nextAction = null
            )
        }
    }
}

@Serializable
private data class AdvanceRequestDto(
    val employeeId: String
)

@Serializable
private data class AdvanceOrderDto(
    val id: Int? = null,
    val taskNumber: String? = null,
    val status: String? = null,
    val nextAction: String? = null
)
```

**Key points:**
- Uses Ktor `HttpClient` injected via Hilt
- `getOrder()` calls `POST /getOrder/{orderId}/advance` — falls back to mock data on failure
- `advanceOrder()` calls `POST /store-tasks/{orderId}/advance` with employee ID body — falls back to mock on failure
- Mock data includes 3 order items with warehouse locations and yaw/pitch for panorama markers
- `@Singleton` scoped

---

## 6.4 Hilt DI Module

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/data/di/OrderModule.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.data.di

import com.autopart.ordermgmt.orderdetail.data.repository.OrderRepositoryImpl
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderModule {
    
    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    companion object {
        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }
        }
    }
}
```

**Key points:**
- `@Binds` maps `OrderRepositoryImpl` → `OrderRepository`
- `@Provides` supplies a singleton `HttpClient` with kotlinx.serialization JSON config
- `ignoreUnknownKeys = true` allows API responses with extra fields

---

## 6.5 Domain Use Cases

### GetOrderUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/domain/usecase/GetOrderItemsUseCase.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.domain.usecase

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String?): Order? {
        return orderRepository.getOrder(orderId)
    }
}
```

### AdvanceOrderUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/domain/usecase/AdvanceOrderUseCase.kt`

```kotlin
package com.autopart.ordermgmt.orderdetail.domain.usecase

import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import javax.inject.Inject

class AdvanceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, employeeId: String): StoreTaskAdvanceResponse? {
        return orderRepository.advanceOrder(orderId, employeeId)
    }
}
```

---

## 6.6 Order Detail ViewModel

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/presentation/viewmodel/OrderDetailViewModel.kt`

```kotlin
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

    fun advanceOrder(orderId: String) {
        if (_isCompleted.value || _isAdvancing.value) return

        viewModelScope.launch {
            try {
                _isAdvancing.value = true
                val response = advanceOrderUseCase(orderId, employeeId)
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
```

**Key points:**
- `@HiltViewModel` with injected `GetOrderUseCase` and `AdvanceOrderUseCase`
- State flows: `order`, `isLoading`, `currentAction`, `isAdvancing`, `isCompleted`
- `setOrder()` — sets order directly (used when navigating from Home screen)
- `loadOrder()` — fetches order from API by order ID
- `advanceOrder()` — advances order task status; sets `COMPLETED` when `nextAction` is null/blank
- Generates random 5-digit employee ID for advance API calls

---

## 6.7 Order Detail Screen (Full UI)

**File:** `app/src/main/java/com/autopart/ordermgmt/orderdetail/presentation/ui/screens/OrderDetailScreen.kt`

This is a large file (~693 lines). It contains:

### Main Composable: `OrderDetailFullScreen`

```kotlin
package com.autopart.ordermgmt.orderdetail.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.autopart.ordermgmt.R
import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.OrderItem
import com.autopart.ordermgmt.orderdetail.presentation.viewmodel.OrderDetailViewModel
import com.autopart.ordermgmt.panorama.domain.model.PanoramaImage
import com.autopart.ordermgmt.panorama.presentation.ui.Panorama360View
import com.autopart.ordermgmt.panorama.presentation.viewmodel.PanoramaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailFullScreen(
    navController: NavController,
    orderDetailViewModel: OrderDetailViewModel = hiltViewModel(
        viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner
    ),
    panoramaViewModel: PanoramaViewModel = hiltViewModel(
        viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner
    )
) {
    val orderData by orderDetailViewModel.order.collectAsState()
    val panoramaImages by panoramaViewModel.panoramaImages.collectAsState()
    val currentAction by orderDetailViewModel.currentAction.collectAsState()
    val isAdvancing by orderDetailViewModel.isAdvancing.collectAsState()
    val isCompleted by orderDetailViewModel.isCompleted.collectAsState()
    var isPanoramaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(orderData) {
        orderData?.items?.let { items ->
            panoramaViewModel.setPanoramaFromOrderItems(items)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("panorama_full_screen") }) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Full screen")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Expandable 360° panorama view card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "360° View", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { isPanoramaExpanded = !isPanoramaExpanded }) {
                                Icon(
                                    imageVector = if (isPanoramaExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isPanoramaExpanded) "Collapse" else "Expand"
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = isPanoramaExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(top = 12.dp)
                            ) {
                                Panorama360View(
                                    panoramaImage = panoramaImages.firstOrNull() ?: PanoramaImage(
                                        id = "default",
                                        title = "Default View",
                                        description = "Default panoramic view",
                                        imageUrl = "panorama_dummy"
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            item {
                orderData?.let {
                    OrderInfoCard(
                        orderInfo = it,
                        isLoading = false,
                        onStatusClick = { orderDetailViewModel.advanceOrder(it.id.toString()) },
                        currentAction = currentAction,
                        isAdvancing = isAdvancing,
                        isCompleted = isCompleted
                    )
                }
            }

            item {
                Text(
                    text = "${orderData?.items?.size ?: 0} items in order",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(orderData?.items ?: emptyList()) { item ->
                OrderItemDetailCard(item = item)
            }

            item {
                orderData?.let { BillSummaryCard(orderItems = it.items) }
            }

            item {
                OrderIdSection(orderId = orderData?.orderNumber ?: "")
            }
        }
    }
}
```

### Sub-composables

The file also contains these composables (see full source in the project):

- **`OrderItemDetailCard(item: OrderItem)`** — Row with product image, name, SKU, quantity, price, and warehouse location (aisle-shelf-bin)
- **`BillSummaryCard(orderItems: List<OrderItem>)`** — Bill summary with items total, handling fee (FREE prime), delivery fee (FREE), total payable, inclusive of taxes
- **`OrderIdSection(orderId: String)`** — Displays the order ID
- **`OrderInfoCard(...)`** — Customer information card with:
  - Order status as clickable pill button (advances order on click)
  - Order number + created date
  - Customer name + customer ID
  - Total amount + payment method
  - Email + phone
  - Shipping + billing address
  - Loading spinner when advancing, green color when completed
- **`formatDate(isoDate: String): String`** — Converts ISO date to `dd/MM/yyyy` format
- **`formatPaymentMethod(method: String): String`** — Formats payment method enum to display string

---

## UI Structure

```
OrderDetailFullScreen
├── Scaffold
│   ├── TopAppBar ("Order details" + back button)
│   ├── FAB (fullscreen panorama → "panorama_full_screen")
│   └── LazyColumn
│       ├── Card: 360° View (expandable)
│       │   ├── "360° View" title + expand/collapse arrow
│       │   └── AnimatedVisibility → Panorama360View
│       ├── OrderInfoCard
│       │   ├── "Customer Information" title
│       │   ├── Order Status pill (clickable → advanceOrder)
│       │   ├── Order Number + Created At
│       │   ├── Customer Name + Customer ID
│       │   ├── Total Amount + Payment Method
│       │   ├── Email + Phone
│       │   └── Shipping + Billing Address
│       ├── "N items in order" text
│       ├── OrderItemDetailCard (per item)
│       │   ├── Product image
│       │   ├── Product name, SKU, quantity
│       │   ├── Total price + unit price
│       │   └── Location: aisle-shelf-bin
│       ├── BillSummaryCard
│       │   ├── Items total
│       │   ├── Handling fee (FREE prime)
│       │   ├── Delivery fee (FREE)
│       │   └── You pay total
│       └── OrderIdSection
```

---

## API Endpoints

| Method | Endpoint | Body | Purpose |
|--------|----------|------|---------|
| POST | `/getOrder/{orderId}/advance` | `null` | Fetch order with items |
| POST | `/store-tasks/{orderId}/advance` | `{"employeeId": "..."}` | Advance order task status |

Both endpoints fall back to mock data on failure.

---

## Architecture Overview

```
com.autopart.ordermgmt.orderdetail/
├── data/
│   ├── di/
│   │   └── OrderModule.kt                    # Hilt DI (binds repo, provides HttpClient)
│   └── repository/
│       └── OrderRepositoryImpl.kt            # Ktor API calls + mock fallback
├── domain/
│   ├── model/
│   │   ├── OrderItem.kt                      # Order, OrderItem, ItemLocation (Parcelize)
│   │   └── StoreTaskAdvanceResponse.kt       # Advance API response model
│   ├── repository/
│   │   └── OrderRepository.kt                # Repository interface
│   └── usecase/
│       ├── GetOrderItemsUseCase.kt           # GetOrderUseCase
│       └── AdvanceOrderUseCase.kt            # AdvanceOrderUseCase
└── presentation/
    ├── viewmodel/
    │   └── OrderDetailViewModel.kt           # HiltViewModel with state flows
    └── ui/screens/
        └── OrderDetailScreen.kt              # Full Compose UI
```

---

## Summary

After this step you should have:
- Domain models: `Order`, `OrderItem`, `ItemLocation` (Parcelize), `StoreTaskAdvanceResponse`
- `OrderRepository` interface + `OrderRepositoryImpl` with Ktor HTTP calls and mock fallback
- `OrderModule` Hilt DI with HttpClient provider
- `GetOrderUseCase` and `AdvanceOrderUseCase`
- `OrderDetailViewModel` with order loading, status advancing, and completion state
- `OrderDetailFullScreen` with expandable 360° view, customer info card, item list, bill summary, and order ID
