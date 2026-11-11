package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.WorkshopTask
import kotlinx.coroutines.flow.Flow
@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Query("SELECT type, SUM(amount) as totalAmount FROM financial_transactions GROUP BY type")
    fun getFinancialSummaryByType(): Flow<List<com.example.data.model.FinancialTypeSummary>>

    @Query("SELECT * FROM financial_transactions")
    suspend fun getAllTransactionsList(): List<FinancialTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinancialTransaction>)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM financial_transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT COUNT(*) FROM financial_transactions")
    suspend fun getTransactionCount(): Int
}

@Dao
interface WorkshopTaskDao {
    @Query("SELECT * FROM workshop_tasks ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END, createdAt DESC")
    fun getAllTasks(): Flow<List<WorkshopTask>>

    @Query("SELECT * FROM workshop_tasks")
    suspend fun getAllTasksList(): List<WorkshopTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WorkshopTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<WorkshopTask>)

    @Update
    suspend fun updateTask(task: WorkshopTask)

    @Delete
    suspend fun deleteTask(task: WorkshopTask)

    @Query("DELETE FROM workshop_tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM workshop_tasks")
    suspend fun getTaskCount(): Int
}

@Dao
interface SupplyItemDao {
    @Query("SELECT * FROM supply_items ORDER BY isNeeded DESC, name ASC")
    fun getAllSupplies(): Flow<List<SupplyItem>>

    @Query("SELECT * FROM supply_items")
    suspend fun getAllSuppliesList(): List<SupplyItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupply(item: SupplyItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplies(items: List<SupplyItem>)

    @Update
    suspend fun updateSupply(item: SupplyItem)

    @Delete
    suspend fun deleteSupply(item: SupplyItem)

    @Query("DELETE FROM supply_items")
    suspend fun deleteAllSupplies()

    @Query("SELECT COUNT(*) FROM supply_items")
    suspend fun getSupplyCount(): Int
}
