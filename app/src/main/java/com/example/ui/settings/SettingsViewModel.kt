package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupStats
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val lastBackupStats: BackupStats? = null,
    val ordersCount: Int = 0,
    val transactionsCount: Int = 0,
    val tasksCount: Int = 0,
    val suppliesCount: Int = 0
)

class SettingsViewModel(private val repository: WorkshopRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allWorkOrdersWithClient,
                repository.allTransactions,
                repository.allTasks,
                repository.allSupplies
            ) { orders, transactions, tasks, supplies ->
                intArrayOf(orders.size, transactions.size, tasks.size, supplies.size)
            }.collect { counts ->
                _uiState.value = _uiState.value.copy(
                    ordersCount = counts[0],
                    transactionsCount = counts[1],
                    tasksCount = counts[2],
                    suppliesCount = counts[3]
                )
            }
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = BackupManager.exportBackupToUri(context, repository, uri)
            result.fold(
                onSuccess = { stats ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "¡Copia de seguridad exportada con éxito!",
                        lastBackupStats = stats
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al exportar: ${err.localizedMessage}"
                    )
                }
            )
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = BackupManager.importBackupFromUri(context, repository, uri)
            result.fold(
                onSuccess = { stats ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "¡Base de datos restaurada correctamente!",
                        lastBackupStats = stats
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al restaurar: ${err.localizedMessage}"
                    )
                }
            )
        }
    }

    fun shareCloudBackup(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            runCatching { BackupManager.createShareBackupIntent(context, repository) }
                .onSuccess { intent ->
                    runCatching { context.startActivity(intent) }
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Copia de seguridad generada y lista para compartir."
                            )
                        }
                        .onFailure { err ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "No se encontró una aplicación para compartir: ${err.localizedMessage}"
                            )
                        }
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al generar la copia: ${err.localizedMessage}"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

class SettingsViewModelFactory(private val repository: WorkshopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
