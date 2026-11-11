package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientEntity
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkOrderWithClient
import com.example.data.model.WorkshopTask
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkshopViewModel(private val repository: WorkshopRepository) : ViewModel() {

    // --- Search & Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- CRM Exact Workflow States ---
    val crmStatusTabs = WorkOrderStatus.values().toList()
    private val _selectedCrmTab = MutableStateFlow(0)
    val selectedCrmTab: StateFlow<Int> = _selectedCrmTab.asStateFlow()

    fun selectCrmTab(index: Int) {
        _selectedCrmTab.value = index
    }

    val allCrmOrders: StateFlow<List<WorkOrderWithClient>> = repository.allWorkOrdersWithClient
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedCrmOrders: StateFlow<List<WorkOrderWithClient>> = combine(
        allCrmOrders,
        _selectedCrmTab,
        _searchQuery
    ) { orders, tabIndex, query ->
        val currentStatus = crmStatusTabs.getOrElse(tabIndex) { WorkOrderStatus.TO_CHECK }
        val statusFiltered = orders.filter { it.order.status == currentStatus.name }
        if (query.isBlank()) {
            statusFiltered
        } else {
            val q = query.trim().lowercase()
            statusFiltered.filter {
                val client = it.client
                it.order.deviceModel.lowercase().contains(q) ||
                it.order.orderCode.lowercase().contains(q) ||
                it.order.serialNumber.lowercase().contains(q) ||
                (client != null && (
                    client.fullName.lowercase().contains(q) ||
                    client.phone.lowercase().contains(q)
                ))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // --- Order Operations ---
    
    // --- Financial State & Operations (Mercado Pago Style) ---
    val allTransactions: StateFlow<List<FinancialTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // ... rest of finance ...
    
    // --- Workshop Tasks ---
    val allTasks: StateFlow<List<WorkshopTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // ... rest of tasks ...
    
    // --- Supply Items ---
    val allSupplies: StateFlow<List<SupplyItem>> = repository.allSupplies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // ... rest of supplies ...
    
    // --- CRM Actions ---
    fun createCrmOrder(
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
        signatureUri: String? = null,
        onCreated: (WorkOrderEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val order = repository.createClientAndOrder(
                firstName = firstName,
                lastName = lastName,
                address = address,
                phone = phone,
                clientNotes = clientNotes,
                deviceModel = deviceModel,
                serialNumber = serialNumber,
                reportedFault = reportedFault,
                internalNotes = internalNotes,
                partsCost = partsCost,
                laborCost = laborCost,
                status = status,
                photosUris = photosUris,
                signatureUri = signatureUri
            )
            onCreated(order)
        }
    }

    fun updateCrmOrderStatus(order: WorkOrderEntity, newStatus: WorkOrderStatus) {
        viewModelScope.launch {
            repository.updateWorkOrderStatus(order, newStatus)
        }
    }

    fun updateCrmOrderCosts(
        order: WorkOrderEntity,
        partsCost: Double,
        laborCost: Double,
        diagnosis: String = order.technicalDiagnosis
    ) {
        viewModelScope.launch {
            repository.updateWorkOrderCosts(order, partsCost, laborCost, diagnosis)
        }
    }

    fun deleteCrmOrder(order: WorkOrderEntity) {
        viewModelScope.launch {
            repository.deleteWorkOrder(order)
        }
    }
}

class WorkshopViewModelFactory(private val repository: WorkshopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkshopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkshopViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
