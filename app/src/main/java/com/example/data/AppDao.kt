package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Query("SELECT * FROM orders ORDER BY isExpress DESC, dropoffDate ASC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Insert
    suspend fun insertOrderHistoryLog(log: OrderHistoryLog)

    @Query("SELECT * FROM machines ORDER BY type ASC, name ASC")
    fun getAllMachines(): Flow<List<Machine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: Machine)
    
    @Update
    suspend fun updateMachine(machine: Machine)

    @Query("SELECT * FROM consumption_logs ORDER BY date DESC")
    fun getAllConsumptionLogs(): Flow<List<ConsumptionLog>>

    @Insert
    suspend fun insertConsumptionLog(log: ConsumptionLog)

    @Query("SELECT * FROM articles ORDER BY name ASC")
    fun getAllArticles(): Flow<List<Article>>

    @Insert
    suspend fun insertArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)
}
