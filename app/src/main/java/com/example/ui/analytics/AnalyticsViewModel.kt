package com.example.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DeviceModelCount
import com.example.data.model.FaultProfitStat
import com.example.data.model.FinancialTypeSummary
import com.example.data.model.RetargetingCandidate
import com.example.data.repository.WorkshopRepository
import com.example.domain.usecase.GetPendingRetargetingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(private val repository: WorkshopRepository) : ViewModel() {

    val topDeviceModels: StateFlow<List<DeviceModelCount>> = repository.topDeviceModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topProfitFaults: StateFlow<List<FaultProfitStat>> = repository.topProfitFaults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<List<FinancialTypeSummary>> = repository.financialSummaryByType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val retargetingCandidates: StateFlow<List<RetargetingCandidate>> = GetPendingRetargetingUseCase(repository).invoke()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class AnalyticsViewModelFactory(private val repository: WorkshopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
