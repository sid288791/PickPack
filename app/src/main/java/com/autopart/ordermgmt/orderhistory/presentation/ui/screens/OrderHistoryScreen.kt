/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderhistory.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    orderIdFromNotification: String? = null,
    orderDetailViewModel: OrderDetailViewModel = hiltViewModel(viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner)
) {
    val order by orderDetailViewModel.order.collectAsState()
    System.out.print("OrderHistoryScreen LaunchedEffect triggered with orderIdFromNotification: $orderIdFromNotification")
    // Load order from repository if order ID is from notification
    LaunchedEffect(orderIdFromNotification) {
        System.out.print("OrderHistoryScreen LaunchedEffect triggered with orderIdFromNotification: $orderIdFromNotification")
        if (orderIdFromNotification != null) {
            System.out.print("Loading order from notification with ID: $orderIdFromNotification")
            orderDetailViewModel.loadOrder(orderIdFromNotification)
        }
        System.out.print("OrderHistoryScreen LaunchedEffect triggered with orderIdFromNotification: $orderIdFromNotification")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            )
        }
    ) { paddingValues ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No orders found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "You have no order history yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                order?.let { orderData ->
                    items(1) {
                        OrderSummaryCard(
                            order = orderData,
                            onViewDetails = {
                                orderDetailViewModel.setOrder(orderData)
                                navController.navigate("order_detail_full")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    order: Order,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Order summary header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order summary",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onViewDetails) {
                    Text("View order details")
                }
            }
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Order info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ORDER ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.orderStatus,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order items horizontal list
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(order.items) { item ->
                    OrderItemCardNew(
                        item = item,
                        onItemClick = onViewDetails
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemCardNew(
    item: OrderItem,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.icin),
                    contentDescription = item.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Qty: ${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
