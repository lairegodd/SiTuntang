package com.kkn.situntang.ui.surat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kkn.situntang.model.SuratRequest
import com.kkn.situntang.repository.SuratRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SuratState {
    object Idle : SuratState()
    object Loading : SuratState()
    data class Success(val message: String) : SuratState()
    data class Error(val message: String) : SuratState()
}

class SuratViewModel(private val repository: SuratRepository = SuratRepository()) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _suratState = MutableStateFlow<SuratState>(SuratState.Idle)
    val suratState: StateFlow<SuratState> = _suratState.asStateFlow()

    private val _myRequests = MutableStateFlow<List<SuratRequest>>(emptyList())
    val myRequests: StateFlow<List<SuratRequest>> = _myRequests.asStateFlow()

    private val _allRequests = MutableStateFlow<List<SuratRequest>>(emptyList())
    val allRequests: StateFlow<List<SuratRequest>> = _allRequests.asStateFlow()

    init {
        loadMyRequests()
        loadAllRequests()
    }

    private fun loadMyRequests() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getMySuratRequests(userId).collect {
                _myRequests.value = it
            }
        }
    }

    fun loadAllRequests() {
        viewModelScope.launch {
            repository.getAllSuratRequests().collect {
                _allRequests.value = it
            }
        }
    }

    fun submitRequest(
        nik: String,
        nama: String,
        tanggalLahir: String,
        alamat: String,
        jenisSurat: String,
        keperluan: String,
        catatan: String,
        metodePengambilan: String
    ) {
        val user = auth.currentUser ?: return
        
        if (nik.length != 16) {
            _suratState.value = SuratState.Error("NIK harus 16 digit")
            return
        }

        val request = SuratRequest(
            userId = user.uid,
            nik = nik,
            nama = nama,
            tanggalLahir = tanggalLahir,
            alamat = alamat,
            jenisSurat = jenisSurat,
            keperluan = keperluan,
            catatan = catatan.ifBlank { null },
            metodePengambilan = metodePengambilan,
            status = "PENDING",
            tanggalPengajuan = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            createdAt = Timestamp.now()
        )

        viewModelScope.launch {
            _suratState.value = SuratState.Loading
            val result = repository.submitSurat(request)
            if (result.isSuccess) {
                _suratState.value = SuratState.Success("Pengajuan surat berhasil dikirim")
            } else {
                _suratState.value = SuratState.Error(result.exceptionOrNull()?.message ?: "Gagal mengirim pengajuan")
            }
        }
    }

    fun approveRequest(requestId: String) {
        val adminId = auth.currentUser?.uid ?: "admin"
        viewModelScope.launch {
            _suratState.value = SuratState.Loading
            val result = repository.approveSurat(requestId, adminId)
            if (result.isSuccess) {
                _suratState.value = SuratState.Success("Pengajuan surat telah DITERIMA")
            } else {
                _suratState.value = SuratState.Error(result.exceptionOrNull()?.message ?: "Gagal memproses pengajuan")
            }
        }
    }

    fun rejectRequest(requestId: String, reason: String) {
        if (reason.isBlank()) {
            _suratState.value = SuratState.Error("Alasan penolakan wajib diisi")
            return
        }
        
        val adminId = auth.currentUser?.uid ?: "admin"
        viewModelScope.launch {
            _suratState.value = SuratState.Loading
            val result = repository.rejectSurat(requestId, reason, adminId)
            if (result.isSuccess) {
                _suratState.value = SuratState.Success("Pengajuan surat telah DITOLAK")
            } else {
                _suratState.value = SuratState.Error(result.exceptionOrNull()?.message ?: "Gagal memproses penolakan")
            }
        }
    }

    fun deleteRequests(ids: List<String>) {
        viewModelScope.launch {
            _suratState.value = SuratState.Loading
            val result = repository.deleteSuratRequests(ids)
            if (result.isSuccess) {
                _suratState.value = SuratState.Success("Data pengajuan berhasil dihapus")
            } else {
                _suratState.value = SuratState.Error(result.exceptionOrNull()?.message ?: "Gagal menghapus data")
            }
        }
    }

    fun resetState() {
        _suratState.value = SuratState.Idle
    }
}
