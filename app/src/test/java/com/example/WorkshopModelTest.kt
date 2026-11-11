package com.example

import com.example.data.model.ClientEntity
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkshopTask
import com.example.ui.daily.ShoppingListUiState
import com.example.ui.daily.TaskUiState
import com.example.ui.finance.PERIOD_ALL
import com.example.ui.finance.PERIOD_PREVIOUS_MONTH
import com.example.ui.finance.PERIOD_THIS_MONTH
import com.example.ui.finance.buildFinanceUiState
import com.example.util.QuotePdfData
import com.example.util.RetroDocumentGenerator
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopModelTest {

    @Test
    fun orderCreation_hasCorrectDefaults() {
        val order = WorkOrderEntity(
            clientId = 1L,
            orderCode = "RS-2026-0046",
            deviceModel = "Super Nintendo (SNES)",
            reportedFault = "Sin video",
            technicalDiagnosis = "Capacitores sulfatados",
            laborCost = 35000.0
        )

        assertEquals(WorkOrderStatus.TO_CHECK.name, order.status)
        assertEquals(0.0, order.totalCost, 0.01)
        assertEquals(90, order.warrantyDays)
        assertTrue(order.entryDate > 0)
        assertTrue(order.photosUris.isEmpty())
    }

    @Test
    fun transactionCreation_incomeAndExpense() {
        val income = FinancialTransaction(
            type = TransactionType.INCOME.name,
            amount = 45000.0,
            category = "Reparación Retro",
            description = "Recap SNES"
        )
        val expense = FinancialTransaction(
            type = TransactionType.EXPENSE.name,
            amount = 8500.0,
            category = "Capacitores & ICs",
            description = "Compra estaño y flux"
        )

        assertEquals("INCOME", income.type)
        assertEquals("EXPENSE", expense.type)
        assertEquals(36500.0, income.amount - expense.amount, 0.01)
    }

    @Test
    fun financeState_filtersCurrentAndPreviousMonth() {
        val now = ZonedDateTime.now()
        val currentDate = now.toInstant().toEpochMilli()
        val previousDate = now.minusMonths(1).toInstant().toEpochMilli()
        val transactions = listOf(
            FinancialTransaction(type = "INCOME", amount = 45000.0, category = "Reparación", description = "Actual", date = currentDate),
            FinancialTransaction(type = "EXPENSE", amount = 8500.0, category = "Insumos", description = "Actual", date = currentDate),
            FinancialTransaction(type = "INCOME", amount = 25000.0, category = "Mantenimiento", description = "Anterior", date = previousDate),
            FinancialTransaction(type = "EXPENSE", amount = 6000.0, category = "Químicos", description = "Anterior", date = previousDate)
        )

        val current = buildFinanceUiState(transactions, true, PERIOD_THIS_MONTH, now)
        assertEquals(2, current.transactions.size)
        assertEquals(45000.0, current.monthlyIncome, 0.01)
        assertEquals(8500.0, current.monthlyExpense, 0.01)
        assertEquals(55500.0, current.totalBalance, 0.01)

        val previous = buildFinanceUiState(transactions, true, PERIOD_PREVIOUS_MONTH, now)
        assertEquals(2, previous.transactions.size)
        assertEquals(25000.0, previous.monthlyIncome, 0.01)
        assertEquals(6000.0, previous.monthlyExpense, 0.01)

        val all = buildFinanceUiState(transactions, true, PERIOD_ALL, now)
        assertEquals(4, all.transactions.size)
        assertEquals(70000.0, all.monthlyIncome, 0.01)
        assertEquals(14500.0, all.monthlyExpense, 0.01)
    }

    @Test
    fun taskPriority_andCompletionToggle() {
        val task = WorkshopTask(title = "Recap audio SNES", priority = TaskPriority.HIGH.name)
        assertFalse(task.isCompleted)
        assertEquals("HIGH", task.priority)
        assertTrue(task.copy(isCompleted = true).isCompleted)
    }

    @Test
    fun supplyItem_neededAndBoughtToggle() {
        val supply = SupplyItem(name = "Alcohol isopropílico", quantity = 2, unit = "litros", isNeeded = true)
        assertTrue(supply.isNeeded)
        assertEquals("Alcohol isopropílico", supply.name)
        assertFalse(supply.copy(isNeeded = false).isNeeded)
    }

    @Test
    fun dailyManagementUiStates_keepCountsAndFiltersIndependent() {
        val tasks = listOf(
            WorkshopTask(title = "Recap SNES", priority = "HIGH", isCompleted = false),
            WorkshopTask(title = "Pasta térmica PS4", priority = "MEDIUM", isCompleted = false),
            WorkshopTask(title = "Limpieza N64", priority = "LOW", isCompleted = true)
        )
        val supplies = listOf(
            SupplyItem(name = "Estaño 60/40", isNeeded = true),
            SupplyItem(name = "Flux Amtech", isNeeded = true),
            SupplyItem(name = "Malla desoldante", isNeeded = false)
        )

        val taskState = TaskUiState(
            tasks = tasks.filter { !it.isCompleted },
            taskFilter = "PENDING",
            pendingTasksCount = tasks.count { !it.isCompleted },
            highPriorityTasksCount = tasks.count { it.priority == "HIGH" && !it.isCompleted },
            completedTasksCount = tasks.count { it.isCompleted }
        )
        val shoppingState = ShoppingListUiState(
            shoppingList = supplies.filter { it.isNeeded },
            shoppingFilter = "NEEDED",
            neededSuppliesCount = supplies.count { it.isNeeded },
            inStockSuppliesCount = supplies.count { !it.isNeeded }
        )

        assertEquals(2, taskState.tasks.size)
        assertEquals(2, taskState.pendingTasksCount)
        assertEquals(1, taskState.highPriorityTasksCount)
        assertEquals(1, taskState.completedTasksCount)
        assertEquals(2, shoppingState.shoppingList.size)
        assertEquals(2, shoppingState.neededSuppliesCount)
        assertEquals(1, shoppingState.inStockSuppliesCount)
    }

    @Test
    fun crmClient_andWorkOrder_modelRelations() {
        val client = ClientEntity(
            id = 10L,
            firstName = "Carlos",
            lastName = "Giménez",
            phone = "+54 9 11 4521-8899",
            address = "Av. Rivadavia 4521"
        )
        assertEquals("Carlos Giménez", client.fullName)

        val workOrder = WorkOrderEntity(
            id = 100L,
            clientId = client.id,
            orderCode = "RS-2026-0041",
            deviceModel = "Super Nintendo (SNES)",
            serialNumber = "UN18290192",
            reportedFault = "Sin video ni audio",
            technicalDiagnosis = "Recap SMD completo",
            status = WorkOrderStatus.TO_CHECK.name,
            partsCost = 15000.0,
            laborCost = 20000.0,
            totalCost = 35000.0
        )

        assertEquals("RS-2026-0041", workOrder.orderCode)
        assertEquals(35000.0, workOrder.totalCost, 0.01)
        assertEquals(WorkOrderStatus.TO_CHECK.name, workOrder.status)
    }

    @Test
    fun retroDocumentGenerator_generatesValidTexts() {
        val client = ClientEntity(firstName = "Carlos", lastName = "Giménez", phone = "+54 9 11 4521-8899")
        val order = WorkOrderEntity(
            clientId = 1L,
            orderCode = "RS-2026-0041",
            deviceModel = "Super Nintendo (SNES)",
            serialNumber = "UN18290192",
            reportedFault = "Sin video",
            technicalDiagnosis = "Recap condensadores",
            partsCost = 15000.0,
            laborCost = 20000.0,
            totalCost = 35000.0,
            warrantyDays = 90
        )

        val receipt = RetroDocumentGenerator.generateIntakeReceipt(order, client)
        assertTrue(receipt.contains("RS-2026-0041"))
        assertTrue(receipt.contains("Carlos Giménez"))
        assertTrue(receipt.contains("Super Nintendo (SNES)"))

        val quote = RetroDocumentGenerator.generateQuoteDocument(order, client)
        assertTrue(quote.contains("PRESUPUESTO TÉCNICO"))
        assertTrue(quote.contains("TOTAL PRESUPUESTADO"))

        val warranty = RetroDocumentGenerator.generateWarrantyCertificate(order, client)
        assertTrue(warranty.contains("CERTIFICADO DE GARANTÍA"))
        assertTrue(warranty.contains("90 DÍAS"))
    }

    @Test
    fun quotePdfData_propertiesAndDefaults() {
        val pdfData = QuotePdfData(
            clientName = "Carlos Giménez",
            clientPhone = "+54 9 11 4521-8899",
            deviceBrand = "Nintendo",
            deviceModel = "Super Nintendo (SNES)",
            serialNumber = "UN18290192",
            reportedFault = "Sin video",
            repairDetail = "Recap SMD completo",
            finalCost = 35000.0,
            quoteCode = "RS-PRE-0042"
        )

        assertEquals("Carlos Giménez", pdfData.clientName)
        assertEquals("Nintendo", pdfData.deviceBrand)
        assertEquals("Super Nintendo (SNES)", pdfData.deviceModel)
        assertEquals(35000.0, pdfData.finalCost, 0.01)
        assertEquals("RS-PRE-0042", pdfData.quoteCode)
    }
}
