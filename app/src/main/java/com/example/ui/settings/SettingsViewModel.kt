package com.example.ui.settings

import android.content.Context
import android.content.Intent
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
        loadCounts()
    }

    private fun loadCounts() {
        viewModelScope.launch {
            val orders = repository.getAllOrdersSync().size
            val txs = repository.getAllTransactionsSync().size
            val tasks = repository.getAllTasksSync().size
            val supplies = repository.getAllSuppliesSync().size
            _uiState.value = _uiState.value.copy(
                ordersCount = orders,
                transactionsCount = txs,
                tasksCount = tasks,
                suppliesCount = supplies
            )
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
                    loadCounts()
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
                    loadCounts()
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

    suspend fun prepareCloudShareIntent(context: Context): Intent {
        return BackupManager.createShareBackupIntent(context, repository)
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
