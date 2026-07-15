package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val lineId: String,
    val type: String, // "Retail" or "B2B"
    val permanentPreferences: String
)

@Serializable
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val status: String, // "Queued", "Washing", "Drying", "Ironing", "Ready"
    val totalPrice: Double,
    val pieceCount: Int,
    val isExpress: Boolean,
    val dropoffDate: Long,
    val pickupDate: Long?,
    val paymentStatus: String, // "Pending", "Paid"
    val lastStatusUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_history_logs")
data class OrderHistoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val statusChangedTo: String,
    val changedByUserId: Int,
    val timestamp: Long
)

@Entity(tableName = "machines")
data class Machine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Washer" or "Dryer"
    val status: String // "Active", "Maintenance", "Defect"
)

@Entity(tableName = "consumption_logs")
data class ConsumptionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String, // e.g. "Detergent", "Softener", "Water", "Electricity"
    val quantity: Double,
    val unit: String, // e.g. "Liters", "kg", "kWh"
    val date: Long
)

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double
)
