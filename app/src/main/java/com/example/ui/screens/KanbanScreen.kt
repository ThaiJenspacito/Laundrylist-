package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Order
import com.example.ui.AppViewModel
import com.example.ui.theme.VibrantRed
import com.example.ui.theme.VibrantRedLight

@Composable
fun KanbanScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    
    val statuses = listOf("Queued", "Washing", "Ironing", "Ready", "Delivered")
    var selectedStatus by remember { mutableStateOf(statuses.first()) }
    
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text(text = "Kanban Board", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))
        
        ScrollableTabRow(
            selectedTabIndex = statuses.indexOf(selectedStatus),
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            edgePadding = 0.dp
        ) {
            statuses.forEachIndexed { index, status ->
                Tab(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    text = { Text(status, fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val filteredOrders = orders.filter { it.status == selectedStatus }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredOrders) { order ->
                OrderCard(order = order, onStatusChange = { newStatus ->
                    viewModel.updateOrderStatus(order, newStatus)
                }, onPartialDelivery = { pieces ->
                    viewModel.deliverPartialOrder(order, pieces)
                }, statuses = statuses)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onStatusChange: (String) -> Unit, onPartialDelivery: (Int) -> Unit, statuses: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var showPartialDialog by remember { mutableStateOf(false) }
    
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    if (showPartialDialog) {
        var partialPieces by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPartialDialog = false },
            title = { Text("Partial Delivery") },
            text = {
                OutlinedTextField(
                    value = partialPieces,
                    onValueChange = { partialPieces = it },
                    label = { Text("Pieces to deliver") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val pieces = partialPieces.toIntOrNull() ?: 0
                    if (pieces > 0 && pieces <= order.pieceCount) {
                        onPartialDelivery(pieces)
                        showPartialDialog = false
                        expanded = false
                    }
                }) {
                    Text("Deliver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (order.isExpress) VibrantRedLight else MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        border = if (order.isExpress) BorderStroke(2.dp, VibrantRed) else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (order.isExpress) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (order.isExpress) {
                    Surface(color = VibrantRed, shape = RoundedCornerShape(4.dp)) {
                        Text("EXPRESS", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pieces: ${order.pieceCount} | Price: ฿${String.format("%.2f", order.totalPrice)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            val timeLimitMs = when (order.status) {
                "Queued" -> 2L * 60 * 60 * 1000
                "Washing" -> 1L * 60 * 60 * 1000
                "Ironing" -> 2L * 60 * 60 * 1000
                "Ready" -> 24L * 60 * 60 * 1000
                else -> 0L
            }
            if (timeLimitMs > 0) {
                val timeInStatus = currentTime - order.lastStatusUpdate
                if (timeInStatus > timeLimitMs) {
                    val overdueMs = timeInStatus - timeLimitMs
                    val overdueHours = overdueMs / (1000 * 60 * 60)
                    val overdueMins = (overdueMs / (1000 * 60)) % 60
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚠️ Overdue by ${overdueHours}h ${overdueMins}m", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showPartialDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Partial Delivery / Split Order")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Move to:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses.filter { it != order.status }) { newStatus ->
                        Button(
                            onClick = { onStatusChange(newStatus) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Text(newStatus)
                        }
                    }
                }
            }
        }
    }
}
