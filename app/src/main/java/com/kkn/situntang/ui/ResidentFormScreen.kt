package com.kkn.situntang.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.situntang.model.Resident
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentFormScreen(viewModel: ResidentViewModel = viewModel(), onBack: () -> Unit = {}) {
    val resident = viewModel.residentState
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()

    var currentSection by remember { mutableIntStateOf(1) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currentDatePickerField by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()

    BackHandler {
        if (currentSection > 1) {
            currentSection--
        } else {
            showCancelDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pendataan - Bagian $currentSection", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSection > 1) currentSection-- else showCancelDialog = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section Progress Indicator
                LinearProgressIndicator(
                    progress = { currentSection / 3f },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                )

                when (currentSection) {
                    1 -> {
                        // Section 1: Data Pelaporan, Data Diri, Kelahiran, Pendidikan & Pekerjaan
                        FormSection(title = "Data Pelaporan") {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                FormTextField("Tgl Pindah Masuk", resident.tanggal_pindah_masuk, modifier = Modifier.weight(1f), readOnly = true)
                                FormTextField("Tanggal Lapor", resident.tanggal_lapor, modifier = Modifier.weight(1f), readOnly = true)
                            }
                        }

                        FormSection(title = "Data Diri") {
                            FormTextField("NIK", resident.nik, icon = Icons.Default.Badge, error = viewModel.nikError) { newValue ->
                                viewModel.updateResident { it.copy(nik = newValue) }
                            }
                            FormTextField("Nama Lengkap (Tanpa Gelar)", resident.nama, icon = Icons.Default.Person, error = viewModel.namaError) { newValue ->
                                viewModel.updateResident { it.copy(nama = newValue) }
                            }
                            
                            Text("Status Kepemilikan Identitas", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("Wajib Identitas", resident.wajib_identitas, modifier = Modifier.weight(1f), readOnly = true)
                                ExposedDropdown("Identitas Elektronik", listOf("Pilih Identitas-EL", "BELUM", "KTP-EL", "KIA"), resident.identitas_elektronik, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(identitas_elektronik = newValue) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdown("Status Rekam", listOf("Pilih Status Rekam", "BELUM REKAM", "SUDAH REKAM", "CARD PRINTED", "PRINT READY RECORD", "CARD SHIPPED", "SENT FOR CARD PRINTING", "CARD ISSUED", "BELUM WAJIB"), resident.status_rekam, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(status_rekam = newValue) }
                                }
                                FormTextField("Tag ID Card", resident.tag_id_card, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(tag_id_card = newValue) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("Nomor KK Sebelumnya", resident.nomor_kk_sebelumnya, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nomor_kk_sebelumnya = newValue) }
                                }
                                ExposedDropdown("Hubungan Dalam Keluarga", listOf("Pilih Hubungan Keluarga", "KEPALA KELUARGA", "SUAMI", "ISTRI", "ANAK", "MENANTU", "CUCU", "ORANG TUA", "MERTUA", "FAMILI LAIN", "PEMBANTU", "LAINNYA"), resident.hubungan_keluarga, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(hubungan_keluarga = newValue) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdown("Jenis Kelamin", listOf("Jenis Kelamin", "Laki-Laki", "Perempuan"), resident.jenis_kelamin, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(jenis_kelamin = newValue) }
                                }
                                ExposedDropdown("Agama", listOf("Pilih Agama", "ISLAM", "KRISTEN", "KATOLIK", "HINDU", "BUDHA", "KHONGHUCU", "KEPERCAYAAN TERHADAP TUHAN YME / LAINNYA"), resident.agama, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(agama = newValue) }
                                }
                            }
                            ExposedDropdown("Status Penduduk", listOf("Pilih Status Penduduk", "TETAP", "TIDAK TETAP"), resident.status_penduduk) { newValue ->
                                viewModel.updateResident { res -> res.copy(status_penduduk = newValue) }
                            }
                        }

                        FormSection(title = "Data Kelahiran") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("Nomor Akta Kelahiran", resident.nomor_akta_kelahiran, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nomor_akta_kelahiran = newValue) }
                                }
                                FormTextField("Tempat Lahir", resident.tempat_lahir, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(tempat_lahir = newValue) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DatePickerField("Tanggal Lahir", resident.tanggal_lahir, error = viewModel.tanggalLahirError, modifier = Modifier.weight(1f)) {
                                    currentDatePickerField = "tanggalLahir"; showDatePicker = true
                                }
                                FormTextField("Waktu Kelahiran", resident.waktu_kelahiran, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(waktu_kelahiran = newValue) }
                                }
                            }
                            ExposedDropdown("Tempat Dilahirkan", listOf("Pilih Tempat Dilahirkan", "RS/RB", "PUSKESMAS", "POLINDES", "RUMAH", "LAINNYA"), resident.tempat_dilahirkan) { newValue ->
                                viewModel.updateResident { res -> res.copy(tempat_dilahirkan = newValue) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdown("Jenis Kelahiran", listOf("Pilih Jenis Kelahiran", "TUNGGAL", "KEMBAR 2", "KEMBAR 3", "KEMBAR 4"), resident.jenis_kelahiran, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(jenis_kelahiran = newValue) }
                                }
                                FormTextField("Anak Ke (Isi dengan angka)", resident.anak_ke, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(anak_ke = newValue) }
                                }
                            }
                            ExposedDropdown("Penolong Kelahiran", listOf("Pilih Penolong Kelahiran", "DOKTER", "BIDAN PERAWAT", "DUKUN", "LAINNYA"), resident.penolong_kelahiran) { newValue ->
                                viewModel.updateResident { res -> res.copy(penolong_kelahiran = newValue) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("Berat Lahir (Gram)", resident.berat_lahir, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(berat_lahir = newValue) }
                                }
                                FormTextField("Panjang Lahir (cm)", resident.panjang_lahir, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(panjang_lahir = newValue) }
                                }
                            }
                        }

                        FormSection(title = "Pendidikan & Pekerjaan") {
                            ExposedDropdown("Pendidikan Dalam KK", listOf("Pilih Pendidikan (Dalam KK)", "TIDAK / BELUM SEKOLAH", "BELUM TAMAT SD/SEDERAJAT", "TAMAT SD / SEDERAJAT", "SLTP/SEDERAJAT", "SLTA / SEDERAJAT", "DIPLOMA I / II", "AKADEMI/ DIPLOMA III/S. MUDA", "DIPLOMA IV/ STRATA I", "STRATA II", "STRATA III"), resident.pendidikan_kk) { newValue ->
                                viewModel.updateResident { res -> res.copy(pendidikan_kk = newValue) }
                            }
                            ExposedDropdown("Pendidikan Sedang Ditempuh", listOf("Pilih Pendidikan", "BELUM MASUK TK/KELOMPOK BERMAIN", "SEDANG TK/KELOMPOK BERMAIN", "TIDAK PERNAH SEKOLAH", "SEDANG SD/SEDERAJAT", "TIDAK TAMAT SD/SEDERAJAT", "SEDANG SLTP/SEDERAJAT", "SEDANG SLTA/SEDERAJAT", "SEDANG D-1/SEDERAJAT", "SEDANG D-2/SEDERAJAT", "SEDANG D-3/SEDERAJAT", "SEDANG S-1/SEDERAJAT", "SEDANG S-2/SEDERAJAT", "SEDANG S-3/SEDERAJAT", "SEDANG SLB A/SEDERAJAT", "SEDANG SLB B/SEDERAJAT", "SEDANG SLB C/SEDERAJAT", "TIDAK DAPAT MEMBACA DAN MENULIS HURUF LATIN/ARAB", "TIDAK SEDANG SEKOLAH"), resident.pendidikan_tempuh) { newValue ->
                                viewModel.updateResident { res -> res.copy(pendidikan_tempuh = newValue) }
                            }
                            FormTextField("Pekerjaan", resident.pekerjaan, icon = Icons.Default.Work) { newValue ->
                                viewModel.updateResident { res -> res.copy(pekerjaan = newValue) }
                            }
                        }
                    }
                    2 -> {
                        // Section 2: Kewarganegaraan, Orang Tua, Alamat
                        FormSection(title = "Data Kewarganegaraan") {
                            ExposedDropdown("Suku/Etnis", listOf("Pilih Suku/Etnis", "Jawa", "Aceh", "Alas", "Alor", "Ambon", "Ampana"), resident.suku_etnis) { newValue ->
                                viewModel.updateResident { res -> res.copy(suku_etnis = newValue) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdown("Status Warga Negara", listOf("Pilih Warga Negara", "WNI", "WNA", "DUA KEWARGANEGARAAN"), resident.status_warga_negara, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(status_warga_negara = newValue) }
                                }
                                FormTextField("Nomor Paspor", resident.nomor_paspor, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nomor_paspor = newValue) }
                                }
                            }
                            DatePickerField("Tgl Berakhir Paspor", resident.tgl_berakhir_paspor) {
                                currentDatePickerField = "tglPaspor"; showDatePicker = true
                            }
                        }

                        FormSection(title = "Data Orang Tua") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("NIK Ayah", resident.nik_ayah, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nik_ayah = newValue) }
                                }
                                FormTextField("Nama Ayah", resident.nama_ayah, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nama_ayah = newValue) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("NIK Ibu", resident.nik_ibu, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nik_ibu = newValue) }
                                }
                                FormTextField("Nama Ibu", resident.nama_ibu, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(nama_ibu = newValue) }
                                }
                            }
                        }

                        FormSection(title = "Alamat") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdown("Dusun", listOf("Pilih Dusun", "Gading", "Daleman", "Cikal", "Petet", "Praguman", "Klurahan"), resident.dusun, modifier = Modifier.weight(2f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(dusun = newValue) }
                                }
                                FormTextField("RW", resident.rw, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(rw = newValue) }
                                }
                                FormTextField("RT", resident.rt, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(rt = newValue) }
                                }
                            }
                            FormTextField("Alamat Sebelumnya", resident.alamat_sebelumnya) { newValue ->
                                viewModel.updateResident { res -> res.copy(alamat_sebelumnya = newValue) }
                            }
                            FormTextField("Alamat Sekarang", resident.alamat, icon = Icons.Default.Home) { newValue ->
                                viewModel.updateResident { res -> res.copy(alamat = newValue) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField("Nomor Telepon", resident.no_telepon, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(no_telepon = newValue) }
                                }
                                FormTextField("Email", resident.email, modifier = Modifier.weight(1f)) { newValue ->
                                    viewModel.updateResident { res -> res.copy(email = newValue) }
                                }
                            }
                            FormTextField("Telegram", resident.telegram) { newValue ->
                                viewModel.updateResident { res -> res.copy(telegram = newValue) }
                            }
                            ExposedDropdown("Cara Hubung Warga", listOf("Pilih Cara Hubungi", "Email", "Telegram"), resident.cara_hubung) { newValue ->
                                viewModel.updateResident { res -> res.copy(cara_hubung = newValue) }
                            }
                        }
                    }
                    3 -> {
                        // Section 3: Status Perkawinan, Kesehatan, Lainnya
                        FormSection(title = "Status Perkawinan") {
                            ExposedDropdown("Status Perkawinan", listOf("Pilih Status Perkawinan", "BELUM KAWIN", "KAWIN", "CERAI HIDUP", "CERAI MATI"), resident.status_perkawinan) { newValue ->
                                viewModel.updateResident { res -> res.copy(status_perkawinan = newValue) }
                            }
                            FormTextField("No. Akta Nikah (Buku Nikah)/Perkawinan", resident.no_akta_nikah) { newValue ->
                                viewModel.updateResident { res -> res.copy(no_akta_nikah = newValue) }
                            }
                            DatePickerField("Tanggal Perkawinan", resident.tanggal_perkawinan) {
                                currentDatePickerField = "tglKawin"; showDatePicker = true
                            }
                            FormTextField("Akta Perceraian", resident.akta_perceraian) { newValue ->
                                viewModel.updateResident { res -> res.copy(akta_perceraian = newValue) }
                            }
                            DatePickerField("Tanggal Perceraian", resident.tanggal_perceraian) {
                                currentDatePickerField = "tglCerai"; showDatePicker = true
                            }
                        }

                        FormSection(title = "Data Kesehatan") {
                            ExposedDropdown("Golongan Darah", listOf("Pilih Golongan Darah", "A", "B", "AB", "O", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "TIDAK TAHU"), resident.golongan_darah) { newValue ->
                                viewModel.updateResident { res -> res.copy(golongan_darah = newValue) }
                            }
                            ExposedDropdown("Cacat", listOf("Pilih Jenis Cacat", "CACAT FISIK", "CACAT NETRA/BUTA", "CACAT RUNGU/WICARA", "CACAT MENTAL/JIWA", "CACAT FISIK DAN MENTAL", "CACAT LAINNYA", "TIDAK CACAT"), resident.cacat) { newValue ->
                                viewModel.updateResident { res -> res.copy(cacat = newValue) }
                            }
                            ExposedDropdown("Sakit Menahun", listOf("Pilih Sakit Menahun", "JANTUNG", "LEVER", "PARU-PARU", "KANKER", "STROKE", "DIABETES MELITUS", "GINJAL", "MALARIA", "LEPRA/KUSTA", "HIV/AIDS", "GILA/STRESS", "TBC", "ASTHMA", "TIDAK ADA/TIDAK SAKIT"), resident.sakit_menahun) { newValue ->
                                viewModel.updateResident { res -> res.copy(sakit_menahun = newValue) }
                            }
                            ExposedDropdown("Akseptor KB", listOf("Pilih Cara KB Saat Ini", "PIL", "IUD", "SUNTIK", "KONDOM", "SUSUK KB", "STERILISASI WANITA", "STERILISASI PRIA", "LAINNYA", "TIDAK MENGGUNAKAN"), resident.akseptor_kb) { newValue ->
                                viewModel.updateResident { res -> res.copy(akseptor_kb = newValue) }
                            }
                            ExposedDropdown("Asuransi Kesehatan", listOf("Pilih Asuransi", "TIDAK/BELUM PUNYA", "BPJS PENERIMA BANTUAN IURAN", "BPJS NON PENERIMA BANTUAN IURAN", "ASURANSI LAINNYA"), resident.asuransi_kesehatan) { newValue ->
                                viewModel.updateResident { res -> res.copy(asuransi_kesehatan = newValue) }
                            }
                            FormTextField("Nomor BPJS Ketenagakerjaan", resident.no_bpjs_ketenagakerjaan) { newValue ->
                                viewModel.updateResident { res -> res.copy(no_bpjs_ketenagakerjaan = newValue) }
                            }
                        }

                        FormSection(title = "Lainnya") {
                            ExposedDropdown("Dapat Membaca Huruf", listOf("Pilih Isian", "LATIN", "DAERAH", "ARAB", "ARAB DAN LATIN", "ARAB DAN DAERAH", "ARAB, LATIN DAN DAERAH"), resident.dapat_membaca_huruf) { newValue ->
                                viewModel.updateResident { res -> res.copy(dapat_membaca_huruf = newValue) }
                            }
                            FormTextField("Keterangan", resident.keterangan) { newValue ->
                                viewModel.updateResident { res -> res.copy(keterangan = newValue) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (currentSection > 1) {
                        OutlinedButton(
                            onClick = { currentSection-- },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("KEMBALI")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentSection < 3) {
                                currentSection++
                            } else {
                                viewModel.saveResident(null)
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (currentSection < 3) "LANJUT" else "SIMPAN DATA", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            if (formState is FormState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pengisian?") },
            text = { Text("Data yang sudah Anda masukkan tidak akan tersimpan.") },
            confirmButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    onBack() 
                }) { Text("Ya, Batal", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Kembali") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(millis))
                        viewModel.updateResident { resident ->
                            when (currentDatePickerField) {
                                "tanggalLahir" -> resident.copy(tanggal_lahir = date)
                                "tglPaspor" -> resident.copy(tgl_berakhir_paspor = date)
                                "tglKawin" -> resident.copy(tanggal_perkawinan = date)
                                "tglCerai" -> resident.copy(tanggal_perceraian = date)
                                else -> resident
                            }
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    modifier: Modifier = Modifier,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormTextField(
    label: String, 
    value: String, 
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    error: String? = null,
    onValueChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}
