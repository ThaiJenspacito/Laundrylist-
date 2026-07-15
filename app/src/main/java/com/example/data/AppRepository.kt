package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allCustomers: Flow<List<Customer>> = appDao.getAllCustomers()
    val allOrders: Flow<List<Order>> = appDao.getAllOrders()
    val allMachines: Flow<List<Machine>> = appDao.getAllMachines()
    val allConsumptionLogs: Flow<List<ConsumptionLog>> = appDao.getAllConsumptionLogs()
    val allArticles: Flow<List<Article>> = appDao.getAllArticles()

    suspend fun insertCustomer(customer: Customer) = appDao.insertCustomer(customer)
    
    suspend fun insertOrder(order: Order) {
        val id = appDao.insertOrder(order)
        appDao.insertOrderHistoryLog(OrderHistoryLog(orderId = id.toInt(), statusChangedTo = order.status, changedByUserId = 1, timestamp = System.currentTimeMillis()))
    }
    
    suspend fun updateOrder(order: Order) {
        appDao.updateOrder(order)
        appDao.insertOrderHistoryLog(OrderHistoryLog(orderId = order.id, statusChangedTo = order.status, changedByUserId = 1, timestamp = System.currentTimeMillis()))
    }

    suspend fun insertMachine(machine: Machine) = appDao.insertMachine(machine)
    suspend fun updateMachine(machine: Machine) = appDao.updateMachine(machine)

    suspend fun insertConsumptionLog(log: ConsumptionLog) = appDao.insertConsumptionLog(log)

    suspend fun insertArticle(article: Article) = appDao.insertArticle(article)
    suspend fun deleteArticle(article: Article) = appDao.deleteArticle(article)
}
