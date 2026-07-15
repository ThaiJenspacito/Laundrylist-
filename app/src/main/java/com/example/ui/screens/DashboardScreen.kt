package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.example.ui.theme.VibrantBlueLight
import com.example.ui.theme.VibrantBlueDark
import com.example.ui.theme.VibrantEmerald
import com.example.ui.theme.VibrantOrange
import com.example.ui.theme.VibrantRedLight
import com.example.ui.theme.VibrantRed

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

fun mapWeatherCodeToIconAndDesc(code: Int): Pair<String, String> {
    return when (code) {
        0 -> "☀️" to "Clear"
        1, 2, 3 -> "⛅" to "Cloudy"
        45, 48 -> "🌫️" to "Fog"
        51, 53, 55, 56, 57 -> "🌧️" to "Drizzle"
        61, 63, 65, 66, 67 -> "🌧️" to "Rain"
        71, 73, 75, 77 -> "❄️" to "Snow"
        80, 81, 82 -> "🌦️" to "Showers"
        85, 86 -> "🌨️" to "Snow Showers"
        95, 96, 99 -> "⛈️" to "Thunderstorm"
        else -> "❓" to "Unknown"
    }
}

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    val weather by viewModel.weatherForecast.collectAsState()
    
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            currentTime = System.currentTimeMillis()
        }
    }

    val queuedCount = orders.count { it.status == "Queued" }
    val washingCount = orders.count { it.status == "Washing" }
    val readyCount = orders.count { it.status == "Ready" }
    
    val overdueOrders = orders.mapNotNull { order ->
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
                order to "Overdue by ${overdueHours}h ${overdueMins}m in ${order.status}"
            } else null
        } else null
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(modifier = Modifier.height(20.dp))
        
        // Weather Section
        if (weather != null && weather!!.daily.time.size >= 4) {
            val daily = weather!!.daily
            val todayCode = daily.weather_code[0]
            val todayMax = daily.temperature_2m_max[0]
            val todayMin = daily.temperature_2m_min[0]
            val (todayIcon, todayDesc) = mapWeatherCodeToIconAndDesc(todayCode)
            
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantBlueLight.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Today's Weather (Large)
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Bangkok, TH", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(todayIcon, style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("${todayMax.toInt()}° / ${todayMin.toInt()}°", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                                Text(todayDesc, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    
                    // Right: Next 3 days (Smaller)
                    Column(modifier = Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..3) {
                            val code = daily.weather_code[i]
                            val (icon, _) = mapWeatherCodeToIconAndDesc(code)
                            val max = daily.temperature_2m_max[i]
                            val dateStr = daily.time[i]
                            val dayOfWeek = try {
                                LocalDate.parse(dateStr).dayOfWeek.name.take(3)
                            } catch(e: Exception) { "Day" }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(dayOfWeek, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(icon, style = MaterialTheme.typography.bodyLarge)
                                Text("${max.toInt()}°", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        } else {
            // Loading or fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(VibrantBlueLight, VibrantBlueDark)))
                    .padding(20.dp)
            ) {
                Text("Loading Weather...", color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Queued", value = queuedCount.toString(), color = VibrantBlueDark, modifier = Modifier.weight(1f))
            StatCard(title = "Washing", value = washingCount.toString(), color = VibrantOrange, modifier = Modifier.weight(1f))
            StatCard(title = "Ready", value = readyCount.toString(), color = VibrantEmerald, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Priority & Alerts", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (overdueOrders.isNotEmpty()) {
                items(overdueOrders) { (order, message) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VibrantRedLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = VibrantRed, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Batch #${order.id}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            val expressOrders = orders.filter { it.isExpress }.take(5)
            if (expressOrders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Express", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                items(expressOrders) { order ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Order #${order.id}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text(order.status, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("EXPRESS PRIORITY", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
