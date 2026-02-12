package com.kkn.situntang.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.situntang.model.Resident
import com.kkn.situntang.repository.ResidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminState {
    object Loading : AdminState()
    data class Success(val list: List<Resident>) : AdminState()
    data class Error(val message: String) : AdminState()
}

class AdminViewModel : ViewModel() {
    private val repository = ResidentRepository()

    private val _adminState = MutableStateFlow<AdminState>(AdminState.Loading)
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    private val _filterStatus = MutableStateFlow("ALL")
    val filterStatus = _filterStatus.asStateFlow()

    init {
        observeResidents()
    }

    private fun observeResidents() {
        viewModelScope.launch {
            repository.getAllResidentsRealtime().collect { residents ->
                _adminState.value = AdminState.Success(residents)
            }
        }
    }

    fun setFilter(status: String) {
        _filterStatus.value = status
    }

    fun approveResident(nik: String) {
        viewModelScope.launch {
            repository.updateResidentStatus(nik, "APPROVED")
        }
    }

    fun rejectResident(nik: String, reason: String) {
        viewModelScope.launch {
            repository.updateResidentStatus(nik, "REJECTED", reason)
        }
    }

    fun deleteResidents(niks: List<String>) {
        viewModelScope.launch {
            repository.deleteResidents(niks)
        }
    }
}
