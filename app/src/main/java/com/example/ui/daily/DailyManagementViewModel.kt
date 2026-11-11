package com.example.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.WorkshopTask
import com.example.data.model.SupplyItem
import com.example.data.model.TaskPriority
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<WorkshopTask> = emptyList(),
    val taskFilter: String = "ALL",
    val pendingTasksCount: Int = 0,
    val highPriorityTasksCount: Int = 0,
    val completedTasksCount: Int = 0
)

data class ShoppingListUiState(
    val shoppingList: List<SupplyItem> = emptyList(),
    val shoppingFilter: String = "ALL",
    val neededSuppliesCount: Int = 0,
    val inStockSuppliesCount: Int = 0
)

class DailyManagementViewModel(private val repository: WorkshopRepository) : ViewModel() {

    private val _taskFilter = MutableStateFlow("ALL")
    private val _shoppingFilter = MutableStateFlow("ALL")

    val tasks: StateFlow<List<WorkshopTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val supplies: StateFlow<List<SupplyItem>> = repository.allSupplies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _taskFilter
    ) { allTasks, filter ->
        val filtered = when (filter) {
            "PENDING" -> allTasks.filter { !it.isCompleted }
            "HIGH" -> allTasks.filter { it.priority == TaskPriority.HIGH.name && !it.isCompleted }
            "COMPLETED" -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }
        TaskUiState(
            tasks = filtered,
            taskFilter = filter,
            pendingTasksCount = allTasks.count { !it.isCompleted },
            highPriorityTasksCount = allTasks.count { it.priority == TaskPriority.HIGH.name && !it.isCompleted },
            completedTasksCount = allTasks.count { it.isCompleted }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskUiState())

    val shoppingUiState: StateFlow<ShoppingListUiState> = combine(
        repository.allSupplies,
        _shoppingFilter
    ) { allSupplies, filter ->
        val filtered = when (filter) {
            "NEEDED" -> allSupplies.filter { it.isNeeded }
            "BOUGHT" -> allSupplies.filter { !it.isNeeded }
            else -> allSupplies
        }
        ShoppingListUiState(
            shoppingList = filtered,
            shoppingFilter = filter,
            neededSuppliesCount = allSupplies.count { it.isNeeded },
            inStockSuppliesCount = allSupplies.count { !it.isNeeded }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShoppingListUiState())

    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun setShoppingFilter(filter: String) {
        _shoppingFilter.value = filter
    }

    fun addTask(title: String, priority: TaskPriority, relatedConsole: String) {
        viewModelScope.launch {
            repository.insertTask(WorkshopTask(title = title, priority = priority.name, relatedConsole = relatedConsole))
        }
    }

    fun quickAddPresetTask(title: String, priority: TaskPriority) {
        viewModelScope.launch {
            repository.insertTask(WorkshopTask(title = title, priority = priority.name, relatedConsole = "Taller General"))
        }
    }

    fun toggleTask(task: WorkshopTask) {
        viewModelScope.launch { repository.toggleTaskCompleted(task) }
    }

    fun deleteTask(task: WorkshopTask) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
    
    fun addShoppingItem(
        name: String,
        category: String,
        quantity: Int,
        unit: String,
        isNeeded: Boolean = true,
        estimatedPrice: Double,
        supplier: String
    ) {
        if (name.isBlank() || category.isBlank() || unit.isBlank() || quantity <= 0 || !estimatedPrice.isFinite() || estimatedPrice < 0.0) return
        viewModelScope.launch {
            repository.insertSupply(
                SupplyItem(
                    name = name.trim(),
                    category = category.trim(),
                    quantity = quantity,
                    unit = unit.trim(),
                    isNeeded = isNeeded,
                    estimatedPrice = estimatedPrice,
                    supplier = supplier.trim()
                )
            )
        }
    }

    fun quickAddPresetSupply(name: String, category: String = "Consumibles", estimatedPrice: Double = 0.0) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            repository.insertSupply(SupplyItem(name = cleanName, category = category, quantity = 1, unit = "u", estimatedPrice = estimatedPrice, isNeeded = true))
        }
    }

    fun toggleSupplyNeeded(item: SupplyItem) {
        viewModelScope.launch { repository.toggleSupplyNeeded(item) }
    }

    fun toggleShoppingItem(item: SupplyItem) {
        viewModelScope.launch { repository.toggleSupplyNeeded(item) }
    }

    fun deleteShoppingItem(item: SupplyItem) {
        viewModelScope.launch { repository.deleteSupply(item) }
    }
}

class DailyManagementViewModelFactory(private val repository: WorkshopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DailyManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
