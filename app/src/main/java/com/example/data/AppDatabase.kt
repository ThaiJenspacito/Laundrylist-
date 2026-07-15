package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Customer::class, Order::class, OrderHistoryLog::class, Machine::class, ConsumptionLog::class, Article::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
