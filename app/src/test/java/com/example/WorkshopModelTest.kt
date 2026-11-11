package com.example

import com.example.data.model.ConsoleOrder
import com.example.data.model.FinancialTransaction
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import com.example.data.model.WorkshopTask
import com.example.ui.finance.FinanceUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopModelTest {

    @Test
    fun orderCreation_hasCorrectDefaults() {
        val order = ConsoleOrder(
            clientName = "Juan Pérez",
            deviceModel = "Super Nintendo (SNES)",
            reportedFault = "Sin video",
            diagnosis = "Capacitores sulfatados",
            finalPrice = 35000.0
        )

        assertEquals("Juan Pérez", order.clientName)
        assertEquals("Super Nintendo (SNES)", order.deviceModel)
        assertEquals(OrderType.REPAIR.name, order.orderType)
        assertEquals(OrderStatus.TO_FIX.name, order.status)
        assertEquals(35000.0, order.finalPrice, 0.01)
        assertTrue(order.entryDate > 0)
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
    fun financeUiState_balanceCalculation() {
        val txs = listOf(
            FinancialTransaction(type = "INCOME", amount = 45000.0, category = "Reparación", description = "PS4"),
            FinancialTransaction(type = "EXPENSE", amount = 8500.0, category = "Insumos", description = "Flux"),
            FinancialTransaction(type = "INCOME", amount = 25000.0, category = "Mantenimiento", description = "PC"),
            FinancialTransaction(type = "EXPENSE", amount = 6000.0, category = "Químicos", description = "Alcohol")
        )

        val totalIncome = txs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val state = FinanceUiState(
            totalBalance = netBalance,
            monthlyIncome = totalIncome,
            monthlyExpense = totalExpense,
            netMonthlyMargin = netBalance,
            transactions = txs
        )

        assertEquals(70000.0, state.monthlyIncome, 0.01)
        assertEquals(14500.0, state.monthlyExpense, 0.01)
        assertEquals(55500.0, state.totalBalance, 0.01)
    }

    @Test
    fun taskPriority_andCompletionToggle() {
        val task = WorkshopTask(
            title = "Recap audio SNES",
            priority = TaskPriority.HIGH.name
        )

        assertFalse(task.isCompleted)
        assertEquals("HIGH", task.priority)

        val completedTask = task.copy(isCompleted = true)
        assertTrue(completedTask.isCompleted)
    }

    @Test
    fun supplyItem_neededAndBoughtToggle() {
        val supply = com.example.data.model.SupplyItem(
            name = "Alcohol isopropílico",
            quantity = 2,
            unit = "litros",
            isNeeded = true
        )

        assertTrue(supply.isNeeded)
        assertEquals("Alcohol isopropílico", supply.name)

        // When bought/checked
        val boughtSupply = supply.copy(isNeeded = false)
        assertFalse(boughtSupply.isNeeded)
    }

    @Test
    fun dailyManagementUiState_countsCalculation() {
        val tasks = listOf(
            WorkshopTask(title = "Recap SNES", priority = "HIGH", isCompleted = false),
            WorkshopTask(title = "Pasta térmica PS4", priority = "MEDIUM", isCompleted = false),
            WorkshopTask(title = "Limpieza N64", priority = "LOW", isCompleted = true)
        )

        val supplies = listOf(
            com.example.data.model.SupplyItem(name = "Estaño 60/40", isNeeded = true),
            com.example.data.model.SupplyItem(name = "Flux Amtech", isNeeded = true),
            com.example.data.model.SupplyItem(name = "Malla desoldante", isNeeded = false)
        )

        val state = com.example.ui.daily.DailyManagementUiState(
            tasks = tasks,
            shoppingList = supplies,
            pendingTasksCount = tasks.count { !it.isCompleted },
            highPriorityTasksCount = tasks.count { it.priority == "HIGH" && !it.isCompleted },
            completedTasksCount = tasks.count { it.isCompleted },
            neededSuppliesCount = supplies.count { it.isNeeded },
            inStockSuppliesCount = supplies.count { !it.isNeeded }
        )

        assertEquals(2, state.pendingTasksCount)
        assertEquals(1, state.highPriorityTasksCount)
        assertEquals(1, state.completedTasksCount)
        assertEquals(2, state.neededSuppliesCount)
        assertEquals(1, state.inStockSuppliesCount)
    }

    @Test
    fun crmClient_andWorkOrder_modelRelations() {
        val client = com.example.data.model.ClientEntity(
            id = 10L,
            firstName = "Carlos",
            lastName = "Giménez",
            phone = "+54 9 11 4521-8899",
            address = "Av. Rivadavia 4521"
        )
        assertEquals("Carlos Giménez", client.fullName)

        val workOrder = com.example.data.model.WorkOrderEntity(
            id = 100L,
            clientId = client.id,
            orderCode = "RS-2026-0041",
            deviceModel = "Super Nintendo (SNES)",
            serialNumber = "UN18290192",
            reportedFault = "Sin video ni audio",
            technicalDiagnosis = "Recap SMD completo",
            status = com.example.data.model.WorkOrderStatus.TO_CHECK.name,
            partsCost = 15000.0,
            laborCost = 20000.0,
            totalCost = 35000.0
        )

        assertEquals("RS-2026-0041", workOrder.orderCode)
        assertEquals(35000.0, workOrder.totalCost, 0.01)
        assertEquals(com.example.data.model.WorkOrderStatus.TO_CHECK.name, workOrder.status)
    }

    @Test
    fun retroDocumentGenerator_generatesValidTexts() {
        val client = com.example.data.model.ClientEntity(
            firstName = "Carlos",
            lastName = "Giménez",
            phone = "+54 9 11 4521-8899"
        )
        val order = com.example.data.model.WorkOrderEntity(
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

        val receipt = com.example.util.RetroDocumentGenerator.generateIntakeReceipt(order, client)
        assertTrue(receipt.contains("RS-2026-0041"))
        assertTrue(receipt.contains("Carlos Giménez"))
        assertTrue(receipt.contains("Super Nintendo (SNES)"))

        val quote = com.example.util.RetroDocumentGenerator.generateQuoteDocument(order, client)
        assertTrue(quote.contains("PRESUPUESTO TÉCNICO"))
        assertTrue(quote.contains("TOTAL PRESUPUESTADO"))

        val warranty = com.example.util.RetroDocumentGenerator.generateWarrantyCertificate(order, client)
        assertTrue(warranty.contains("CERTIFICADO DE GARANTÍA"))
        assertTrue(warranty.contains("90 DÍAS"))
    }

    @Test
    fun quotePdfData_propertiesAndDefaults() {
        val pdfData = com.example.util.QuotePdfData(
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
