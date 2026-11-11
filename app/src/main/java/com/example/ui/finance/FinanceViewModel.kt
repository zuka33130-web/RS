package com.example.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FinancialTransaction
import com.example.data.model.TransactionType
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceUiState(
    val transactions: List<FinancialTransaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val showBalance: Boolean = true,
    val selectedPeriod: String = "Mes"
)

class FinanceViewModel(private val repository: WorkshopRepository) : ViewModel() {

    private val _showBalance = MutableStateFlow(true)
    private val _selectedPeriod = MutableStateFlow("Mes")

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        _showBalance,
        _selectedPeriod
    ) { txs, showBal, period ->
        val income = txs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val expense = txs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val balance = income - expense
        val byCategory = txs.filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        FinanceUiState(
            transactions = txs,
            totalBalance = balance,
            monthlyIncome = income,
            monthlyExpense = expense,
            expensesByCategory = byCategory,
            showBalance = showBal,
            selectedPeriod = period
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceUiState())

    fun toggleBalanceVisibility() {
        _showBalance.value = !_showBalance.value
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun addTransaction(type: TransactionType, amount: Double, category: String, description: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinancialTransaction(type = type.name, amount = amount, category = category, description = description)
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
