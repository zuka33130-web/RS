package com.example.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FinancialTransaction
import com.example.data.model.TransactionType
import com.example.data.repository.WorkshopRepository
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val PERIOD_THIS_MONTH = "ESTE_MES"
const val PERIOD_PREVIOUS_MONTH = "MES_ANTERIOR"
const val PERIOD_ALL = "TODOS"

data class FinanceUiState(
    val transactions: List<FinancialTransaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val expensesByCategory: List<Pair<String, Double>> = emptyList(),
    val showBalance: Boolean = true,
    val selectedPeriod: String = PERIOD_THIS_MONTH,
    val periodLabel: String = "Mes actual"
)

internal fun buildFinanceUiState(
    allTransactions: List<FinancialTransaction>,
    showBalance: Boolean,
    period: String,
    now: ZonedDateTime = ZonedDateTime.now()
): FinanceUiState {
    val selectedMonth = when (period) {
        PERIOD_PREVIOUS_MONTH -> YearMonth.from(now).minusMonths(1)
        else -> YearMonth.from(now)
    }
    val zone = ZoneId.systemDefault()
    val filtered = if (period == PERIOD_ALL) {
        allTransactions
    } else {
        allTransactions.filter { transaction ->
            val transactionMonth = YearMonth.from(
                java.time.Instant.ofEpochMilli(transaction.date).atZone(zone)
            )
            transactionMonth == selectedMonth
        }
    }

    val income = filtered.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    val expense = filtered.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    val totalIncome = allTransactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    val totalExpense = allTransactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    val byCategory = filtered
        .filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.category }
        .map { (category, transactions) -> category to transactions.sumOf { it.amount } }
        .sortedByDescending { it.second }

    return FinanceUiState(
        transactions = filtered,
        totalBalance = totalIncome - totalExpense,
        monthlyIncome = income,
        monthlyExpense = expense,
        expensesByCategory = byCategory,
        showBalance = showBalance,
        selectedPeriod = period,
        periodLabel = when (period) {
            PERIOD_PREVIOUS_MONTH -> "Mes anterior"
            PERIOD_ALL -> "Todo el historial"
            else -> "Mes actual"
        }
    )
}

class FinanceViewModel(private val repository: WorkshopRepository) : ViewModel() {

    private val _showBalance = MutableStateFlow(true)
    private val _selectedPeriod = MutableStateFlow(PERIOD_THIS_MONTH)

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        _showBalance,
        _selectedPeriod
    ) { transactions, showBalance, period ->
        buildFinanceUiState(transactions, showBalance, period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceUiState())

    fun toggleBalanceVisibility() {
        _showBalance.value = !_showBalance.value
    }

    fun setPeriod(period: String) {
        if (period in setOf(PERIOD_THIS_MONTH, PERIOD_PREVIOUS_MONTH, PERIOD_ALL)) {
            _selectedPeriod.value = period
        }
    }

    fun addTransaction(type: TransactionType, amount: Double, category: String, description: String) {
        if (!amount.isFinite() || amount <= 0.0 || category.isBlank()) return
        viewModelScope.launch {
            repository.insertTransaction(
                FinancialTransaction(
                    type = type.name,
                    amount = amount,
                    category = category.trim(),
                    description = description.trim()
                )
            )
        }
    }

    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

class FinanceViewModelFactory(private val repository: WorkshopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
