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
    orderDetailViewModel: OrderDetailViewModel = hiltViewModel(viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner),
    panoramaViewModel: PanoramaViewModel = hiltViewModel(viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner)
) {
    val orderData by orderDetailViewModel.order.collectAsState()
    val panoramaImages by panoramaViewModel.panoramaImages.collectAsState()
    val currentAction by orderDetailViewModel.currentAction.collectAsState()
    val isAdvancing by orderDetailViewModel.isAdvancing.collectAsState()
    val isCompleted by orderDetailViewModel.isCompleted.collectAsState()
    var isPanoramaExpanded by remember { mutableStateOf(false) }

    // Update panorama view with order item locations
    LaunchedEffect(orderData) {
        orderData?.items?.let { items ->
            panoramaViewModel.setPanoramaFromOrderItems(items)
        }
    }
    
    Scaffold(
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = { Text("Order details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
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
            // Normal view with scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                // Expandable panorama view
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "360° View",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { isPanoramaExpanded = !isPanoramaExpanded }) {
                                Icon(
                                    imageVector = if (isPanoramaExpanded) 
                                        Icons.Default.KeyboardArrowUp
                                    else 
                                        Icons.Default.KeyboardArrowDown,
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
                        onStatusClick = {
                            orderDetailViewModel.advanceOrder(it.items.firstOrNull()?.taskId ?: it.id.toString())
                        },
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



@Composable
fun OrderItemDetailCard(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icin),
                    contentDescription = item.sku,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Product details
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp, end = 8.dp)

            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                Text(
                    text = "SKU: ${item.sku} • Qty: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${item.totalPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "@ $${item.unitPrice} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Location info
                Text(
                    text = "Location: ${item.location.aisle}-${item.location.shelf}-${item.location.bin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BillSummaryCard(orderItems: List<OrderItem>) {
    val itemsTotal = orderItems.sumOf { it.totalPrice }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bill summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Items total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items total",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${String.format("%.2f", itemsTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Handling fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Handling fee",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "prime",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$40",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delivery fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivery fee",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$30",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // You pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You pay",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${String.format("%.2f", itemsTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Inclusive of taxes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderIdSection(orderId: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Order ID",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = orderId,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OrderInfoCard(
    orderInfo: Order,
    isLoading: Boolean,
    onStatusClick: () -> Unit,
    currentAction: String = orderInfo.orderStatus,
    isAdvancing: Boolean = false,
    isCompleted: Boolean = false
) {
    val statusColor = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    val statusBgColor = if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Customer Information",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Order Status - styled as visual button with outline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Status",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = statusColor,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(enabled = !isLoading && !isAdvancing && !isCompleted) { onStatusClick() },
                    shape = RoundedCornerShape(20.dp),
                    color = statusBgColor
                ) {
                    if (isAdvancing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = statusColor
                        )
                    } else {
                        Text(
                            text = currentAction,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Number and Created At in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order Number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.orderNumber,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Created At",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(orderInfo.createdAt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name and Customer ID in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Customer Name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.customerName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Customer ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.customerId.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Amount and Currency in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${orderInfo.currency}${String.format("%.2f", orderInfo.totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPaymentMethod(orderInfo.paymentMethod),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email and Phone in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.customerEmail,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Phone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.customerPhone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shipping and Billing Address in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Shipping Address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.shippingAddress,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Billing Address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderInfo.billingAddress,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

fun formatDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("T")
        if (parts.isNotEmpty()) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) {
                "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
            } else {
                isoDate
            }
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}

fun formatPaymentMethod(method: String): String {
    return when (method) {
        "CREDIT_CARD" -> "Credit Card"
        "DEBIT_CARD" -> "Debit Card"
        "PAYPAL" -> "PayPal"
        "CASH" -> "Cash"
        else -> method
    }
}
