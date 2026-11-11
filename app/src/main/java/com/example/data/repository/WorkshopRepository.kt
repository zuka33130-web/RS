package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithOrders
import com.example.data.model.DeviceModelCount
import com.example.data.model.FaultProfitStat
import com.example.data.model.FinancialTransaction
import com.example.data.model.FinancialTypeSummary
import com.example.data.model.SupplyItem
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkOrderWithClient
import com.example.data.model.WorkshopTask
import kotlinx.coroutines.flow.Flow

class WorkshopRepository(private val database: AppDatabase) {

    private val transactionDao = database.financialTransactionDao()
    private val taskDao = database.workshopTaskDao()
    private val supplyDao = database.supplyItemDao()
    private val clientDao = database.clientDao()
    private val workOrderDao = database.workOrderDao()

    // --- CRM Clients & Work Orders ---
    val allWorkOrdersWithClient: Flow<List<WorkOrderWithClient>> =
        workOrderDao.getAllOrdersWithClient()

    fun getOrdersWithClientByStatus(status: String): Flow<List<WorkOrderWithClient>> =
        workOrderDao.getOrdersWithClientByStatus(status)

    val topDeviceModels: Flow<List<DeviceModelCount>> = workOrderDao.getTopDeviceModels()
    val topProfitFaults: Flow<List<FaultProfitStat>> = workOrderDao.getTopProfitFaults()
    val financialSummaryByType: Flow<List<FinancialTypeSummary>> = transactionDao.getFinancialSummaryByType()

    suspend fun getWorkOrderWithClientById(id: Long): WorkOrderWithClient? =
        workOrderDao.getOrderWithClientById(id)

    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()

    suspend fun createClientAndOrder(
        firstName: String,
        lastName: String,
        address: String,
        phone: String,
        clientNotes: String,
        deviceModel: String,
        serialNumber: String,
        reportedFault: String,
        internalNotes: String,
        partsCost: Double = 0.0,
        laborCost: Double = 0.0,
        status: WorkOrderStatus = WorkOrderStatus.TO_CHECK,
        photosUris: List<String> = emptyList(),
        signatureUri: String? = null
    ): WorkOrderEntity {
        val cleanPhone = phone.trim()
        val existingClient = if (cleanPhone.isNotBlank()) clientDao.findClientByPhone(cleanPhone) else null
        val clientId = existingClient?.id ?: run {
            val newClient = ClientEntity(
                firstName = firstName.trim().ifBlank { "Cliente" },
                lastName = lastName.trim(),
                address = address.trim(),
                phone = cleanPhone,
                notes = clientNotes.trim()
            )
            clientDao.insertClient(newClient)
        }

        val totalOrdersCount = workOrderDao.getOrderCount() + 1
        val codeYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val orderCode = "RS-$codeYear-%04d".format(totalOrdersCount)

        val total = partsCost + laborCost
        val order = WorkOrderEntity(
            clientId = clientId,
            orderCode = orderCode,
            deviceModel = deviceModel.trim(),
            serialNumber = serialNumber.trim(),
            reportedFault = reportedFault.trim(),
            internalNotes = internalNotes.trim(),
            status = status.name,
            partsCost = partsCost,
            laborCost = laborCost,
            totalCost = total,
            entryDate = System.currentTimeMillis(),
            photosUris = photosUris,
            signatureUri = signatureUri
        )
        val orderId = workOrderDao.insertOrder(order)
        return order.copy(id = orderId)
    }

    suspend fun updateWorkOrderStatus(order: WorkOrderEntity, newStatus: WorkOrderStatus) {
        val now = System.currentTimeMillis()
        val updated = order.copy(
            status = newStatus.name,
            quotedDate = if (newStatus == WorkOrderStatus.QUOTED && order.quotedDate == null) now else order.quotedDate,
            deliveryDate = if (newStatus == WorkOrderStatus.DELIVERED && order.deliveryDate == null) now else order.deliveryDate
        )
        workOrderDao.updateOrder(updated)

        if (newStatus == WorkOrderStatus.DELIVERED && updated.totalCost > 0) {
            val tx = FinancialTransaction(
                type = TransactionType.INCOME.name,
                amount = updated.totalCost,
                category = "Cobro Reparación CRM",
                description = "${order.orderCode} - ${order.deviceModel}",
                date = now,
                relatedOrderId = order.id
            )
            transactionDao.insertTransaction(tx)
        }
    }

    suspend fun updateWorkOrderCosts(
        order: WorkOrderEntity,
        partsCost: Double,
        laborCost: Double,
        diagnosis: String = order.technicalDiagnosis
    ) {
        val total = partsCost + laborCost
        val updated = order.copy(
            partsCost = partsCost,
            laborCost = laborCost,
            totalCost = total,
            technicalDiagnosis = diagnosis,
            quotedDate = if (order.quotedDate == null) System.currentTimeMillis() else order.quotedDate
        )
        workOrderDao.updateOrder(updated)
    }

    suspend fun updateWorkOrder(order: WorkOrderEntity) {
        workOrderDao.updateOrder(order)
    }

    suspend fun deleteWorkOrder(order: WorkOrderEntity) {
        workOrderDao.deleteOrder(order)
    }

    // --- Financial Transactions ---
    val allTransactions: Flow<List<FinancialTransaction>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: FinancialTransaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: FinancialTransaction) =
        transactionDao.deleteTransaction(transaction)

    // --- Workshop Tasks ---
    val allTasks: Flow<List<WorkshopTask>> = taskDao.getAllTasks()

    suspend fun insertTask(task: WorkshopTask): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: WorkshopTask) = taskDao.updateTask(task)

    suspend fun deleteTask(task: WorkshopTask) = taskDao.deleteTask(task)

    suspend fun toggleTaskCompleted(task: WorkshopTask) {
        taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    // --- Supply Items ---
    val allSupplies: Flow<List<SupplyItem>> = supplyDao.getAllSupplies()

    suspend fun insertSupply(item: SupplyItem): Long = supplyDao.insertSupply(item)

    suspend fun updateSupply(item: SupplyItem) = supplyDao.updateSupply(item)

    suspend fun deleteSupply(item: SupplyItem) = supplyDao.deleteSupply(item)

    suspend fun toggleSupplyNeeded(item: SupplyItem) {
        supplyDao.updateSupply(item.copy(isNeeded = !item.isNeeded))
    }

    // --- Backup & Restore Sync Access ---
    suspend fun getAllOrdersSync(): List<WorkOrderEntity> = workOrderDao.getAllOrdersList()
    suspend fun getAllTransactionsSync(): List<FinancialTransaction> = transactionDao.getAllTransactionsList()
    suspend fun getAllTasksSync(): List<WorkshopTask> = taskDao.getAllTasksList()
    suspend fun getAllSuppliesSync(): List<SupplyItem> = supplyDao.getAllSuppliesList()

    suspend fun restoreAllData(
        orders: List<WorkOrderEntity>,
        transactions: List<FinancialTransaction>,
        tasks: List<WorkshopTask>,
        supplies: List<SupplyItem>
    ) {
        workOrderDao.deleteAllOrders()
        transactionDao.deleteAllTransactions()
        taskDao.deleteAllTasks()
        supplyDao.deleteAllSupplies()

        if (orders.isNotEmpty()) workOrderDao.insertOrders(orders)
        if (transactions.isNotEmpty()) transactionDao.insertTransactions(transactions)
        if (tasks.isNotEmpty()) taskDao.insertTasks(tasks)
        if (supplies.isNotEmpty()) supplyDao.insertSupplies(supplies)
    }
}
