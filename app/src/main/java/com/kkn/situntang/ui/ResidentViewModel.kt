package com.kkn.situntang.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kkn.situntang.model.Resident
import com.kkn.situntang.repository.ResidentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Success : FormState()
    data class Error(val message: String) : FormState()
}

class ResidentViewModel : ViewModel() {
    private val repository = ResidentRepository()
    private val auth = FirebaseAuth.getInstance()
    private var observationJob: Job? = null

    var residentState by mutableStateOf(createNewResident())
        private set

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState = _formState.asStateFlow()

    private val _mySubmissions = MutableStateFlow<List<Resident>>(emptyList())
    val mySubmissions = _mySubmissions.asStateFlow()

    private val _mySubmission = MutableStateFlow<Resident?>(null)
    val mySubmission = _mySubmission.asStateFlow()

    var nikError by mutableStateOf<String?>(null)
    var namaError by mutableStateOf<String?>(null)
    var tanggalLahirError by mutableStateOf<String?>(null)

    init {
        startObservation()
    }

    private fun createNewResident(): Resident {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        return Resident(
            tanggal_pindah_masuk = today,
            tanggal_lapor = today,
            // Inisialisasi string kosong alih-alih placeholder "Pilih..." agar user tidak perlu menghapus teks secara manual
            rw = "",
            rt = "",
            pekerjaan = "",
            dusun = "Pilih Dusun",
            agama = "Pilih Agama",
            jenis_kelamin = "Jenis Kelamin",
            identitas_elektronik = "Pilih Identitas-EL",
            status_rekam = "Pilih Status Rekam",
            hubungan_keluarga = "Pilih Hubungan Keluarga",
            status_penduduk = "Pilih Status Penduduk",
            tempat_dilahirkan = "Pilih Tempat Dilahirkan",
            jenis_kelahiran = "Pilih Jenis Kelahiran",
            penolong_kelahiran = "Pilih Penolong Kelahiran",
            pendidikan_kk = "Pilih Pendidikan (Dalam KK)",
            pendidikan_tempuh = "Pilih Pendidikan",
            suku_etnis = "Pilih Suku/Etnis",
            status_warga_negara = "Pilih Warga Negara",
            cara_hubung = "Pilih Cara Hubungi",
            status_perkawinan = "Pilih Status Perkawinan",
            golongan_darah = "Pilih Golongan Darah",
            cacat = "Pilih Jenis Cacat",
            sakit_menahun = "Pilih Sakit Menahun",
            akseptor_kb = "Pilih Cara KB Saat Ini",
            asuransi_kesehatan = "Pilih Asuransi",
            dapat_membaca_huruf = "Pilih Isian"
        )
    }

    private fun startObservation() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            repository.getMySubmissionRealtime().collect { list ->
                _mySubmissions.value = list
                _mySubmission.value = list.firstOrNull()
            }
        }
    }

    fun refreshUser() {
        _mySubmissions.value = emptyList()
        _mySubmission.value = null
        startObservation()
    }

    fun updateResident(update: (Resident) -> Resident) {
        residentState = update(residentState)
        validateRealtime()
    }

    private fun validateRealtime() {
        if (residentState.nik.isNotEmpty() && residentState.nik.length != 16) {
            nikError = "NIK harus 16 digit"
        } else {
            nikError = null
        }
        
        if (residentState.nama.isNotEmpty()) {
            namaError = null
        }
    }

    fun saveResident(imageUri: Uri?) {
        if (!validateAll()) return

        viewModelScope.launch {
            _formState.value = FormState.Loading
            val result = repository.saveResident(residentState, imageUri)
            if (result.isSuccess) {
                _formState.value = FormState.Success
            } else {
                _formState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetForm() {
        residentState = createNewResident()
        nikError = null
        namaError = null
        tanggalLahirError = null
        _formState.value = FormState.Idle
    }

    private fun validateAll(): Boolean {
        var isValid = true
        if (residentState.nik.length != 16) {
            nikError = "NIK harus 16 digit"
            isValid = false
        }
        if (residentState.nama.isBlank()) {
            namaError = "Nama tidak boleh kosong"
            isValid = false
        }
        if (residentState.tanggal_lahir.isBlank()) {
            tanggalLahirError = "Tanggal lahir wajib diisi"
            isValid = false
        }
        return isValid
    }
}
