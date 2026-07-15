package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.Customer
import com.example.data.Machine
import com.example.data.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.WeatherResponse
import com.example.data.WeatherApi

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    private val weatherApi = WeatherApi.create()

    private val _weatherForecast = MutableStateFlow<WeatherResponse?>(null)
    val weatherForecast: StateFlow<WeatherResponse?> = _weatherForecast

    init {
        fetchWeather()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val response = weatherApi.getDailyForecast()
                _weatherForecast.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val machines: StateFlow<List<Machine>> = repository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consumptionLogs: StateFlow<List<com.example.data.ConsumptionLog>> = repository.allConsumptionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles: StateFlow<List<com.example.data.Article>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertCustomer(name: String, phone: String, lineId: String, preferences: String) {
        viewModelScope.launch {
            repository.insertCustomer(
                Customer(
                    name = name,
                    phone = phone,
                    lineId = lineId,
                    type = "Retail",
                    permanentPreferences = preferences
                )
            )
        }
    }

    fun insertOrder(pieceCount: Int, isExpress: Boolean) {
        val basePrice = pieceCount * (750.0 / 60.0)
        val finalPrice = if (isExpress) basePrice * 1.5 else basePrice
        
        viewModelScope.launch {
            repository.insertOrder(
                Order(
                    status = "Queued",
                    totalPrice = finalPrice,
                    pieceCount = pieceCount,
                    isExpress = isExpress,
                    dropoffDate = System.currentTimeMillis(),
                    pickupDate = null,
                    paymentStatus = "Pending"
                )
            )
        }
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = newStatus, lastStatusUpdate = System.currentTimeMillis()))
        }
    }

    fun updateMachineStatus(machine: Machine, newStatus: String) {
        viewModelScope.launch {
            repository.updateMachine(machine.copy(status = newStatus))
        }
    }

    fun deliverPartialOrder(order: Order, piecesToDeliver: Int) {
        if (piecesToDeliver <= 0 || piecesToDeliver > order.pieceCount) return
        
        val remainingPieces = order.pieceCount - piecesToDeliver
        
        viewModelScope.launch {
            if (remainingPieces > 0) {
                val remainingBasePrice = remainingPieces * (750.0 / 60.0)
                val remainingFinalPrice = if (order.isExpress) remainingBasePrice * 1.5 else remainingBasePrice
                repository.updateOrder(order.copy(
                    pieceCount = remainingPieces,
                    totalPrice = remainingFinalPrice
                ))
                
                val deliveredBasePrice = piecesToDeliver * (750.0 / 60.0)
                val deliveredFinalPrice = if (order.isExpress) deliveredBasePrice * 1.5 else deliveredBasePrice
                repository.insertOrder(order.copy(
                    id = 0,
                    status = "Delivered",
                    pieceCount = piecesToDeliver,
                    totalPrice = deliveredFinalPrice,
                    pickupDate = System.currentTimeMillis()
                ))
            } else {
                repository.updateOrder(order.copy(status = "Delivered", pickupDate = System.currentTimeMillis()))
            }
        }
    }

    fun markOrderAsPaid(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(paymentStatus = "Paid"))
        }
    }

    fun addConsumption(itemName: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            repository.insertConsumptionLog(com.example.data.ConsumptionLog(
                itemName = itemName,
                quantity = quantity,
                unit = unit,
                date = System.currentTimeMillis()
            ))
        }
    }

    fun insertArticle(name: String, price: Double) {
        viewModelScope.launch {
            repository.insertArticle(com.example.data.Article(name = name, price = price))
        }
    }

    fun deleteArticle(article: com.example.data.Article) {
        viewModelScope.launch {
            repository.deleteArticle(article)
        }
    }
}
