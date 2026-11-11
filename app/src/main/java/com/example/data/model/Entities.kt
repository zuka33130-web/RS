package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Transacción financiera (Mercado Pago style)
 */
@Entity(tableName = "financial_transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String, // e.g., "Cobro Reparación", "Venta Consola", "Repuestos", "Insumos Taller", "Herramientas"
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val relatedOrderId: Long? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Tarea diaria To-Do con prioridades
 */
@Entity(tableName = "workshop_tasks")
data class WorkshopTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val priority: String = TaskPriority.MEDIUM.name, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val relatedConsole: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskPriority(val label: String) {
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja")
}

/**
 * Insumo o repuesto de la lista de compras del taller
 */
@Entity(tableName = "supply_items")
data class SupplyItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Consumibles",
    val quantity: Int = 1,
    val unit: String = "u",
    val isNeeded: Boolean = true, // true: falta comprar, false: en stock
    val estimatedPrice: Double = 0.0,
    val supplier: String = ""
)
