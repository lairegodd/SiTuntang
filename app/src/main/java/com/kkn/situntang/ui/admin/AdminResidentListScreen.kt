package com.kkn.situntang.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.situntang.model.Resident

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResidentListScreen(
    viewModel: AdminViewModel = viewModel(),
    onBack: () -> Unit
) {
    val adminState by viewModel.adminState.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedNiks = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val allList = if (adminState is AdminState.Success) (adminState as AdminState.Success).list else emptyList()
    val filteredList = if (filterStatus == "ALL") allList else allList.filter { it.status == filterStatus }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Data Warga") },
            text = { Text("Apakah Anda yakin ingin menghapus ${selectedNiks.size} data warga ini secara permanen? Data yang dihapus tidak dapat dipulihkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteResidents(selectedNiks.toList())
                        isSelectionMode = false
                        selectedNiks.clear()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedNiks.size} Terpilih", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSelectionMode = false
                            selectedNiks.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Batal", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (selectedNiks.size == filteredList.size) {
                                selectedNiks.clear()
                            } else {
                                selectedNiks.clear()
                                selectedNiks.addAll(filteredList.map { it.nik })
                            }
                        }) {
                            Text(if (selectedNiks.size == filteredList.size) "Batal Semua" else "Pilih Semua", color = Color.White)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00838F))
                )
            } else {
                TopAppBar(
                    title = { Text("Data Warga", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF00BCD4),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            FilterTabs(
                selectedFilter = filterStatus,
                onFilterSelected = { 
                    viewModel.setFilter(it)
                    isSelectionMode = false
                    selectedNiks.clear()
                }
            )

            when (adminState) {
                is AdminState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00BCD4))
                    }
                }
                is AdminState.Success -> {
                    if (filteredList.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList) { resident ->
                                ResidentItemCard(
                                    resident = resident,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedNiks.contains(resident.nik),
                                    onApprove = { viewModel.approveResident(resident.nik) },
                                    onReject = { reason -> viewModel.rejectResident(resident.nik, reason) },
                                    onLongClick = {
                                        isSelectionMode = true
                                        selectedNiks.add(resident.nik)
                                    },
                                    onToggleSelection = {
                                        if (selectedNiks.contains(resident.nik)) {
                                            selectedNiks.remove(resident.nik)
                                            if (selectedNiks.isEmpty()) isSelectionMode = false
                                        } else {
                                            selectedNiks.add(resident.nik)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is AdminState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((adminState as AdminState.Error).message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ResidentItemCard(
    resident: Resident,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onLongClick: () -> Unit,
    onToggleSelection: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (resident.status) {
        "PENDING" -> Color(0xFFFFA000)
        "APPROVED" -> Color(0xFF388E3C)
        "REJECTED" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelection() else expanded = !expanded },
                onLongClick = { if (!isSelectionMode) onLongClick() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0F7FA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00BCD4)) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(resident.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text("NIK: ${resident.nik}", fontSize = 13.sp, color = Color.Gray)
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            resident.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isSelectionMode) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = expanded && !isSelectionMode) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // TANGGAL PELAPORAN
                        DetailSection(title = "TANGGAL PELAPORAN")
                        DetailItem(label = "Tanggal Pindah Masuk", value = resident.tanggal_pindah_masuk)
                        DetailItem(label = "Tanggal Lapor", value = resident.tanggal_lapor)
                        
                        // DATA DIRI
                        DetailSection(title = "DATA DIRI")
                        DetailItem(label = "Nama Lengkap", value = resident.nama)
                        DetailItem(label = "NIK", value = resident.nik)
                        DetailItem(label = "Tempat, Tgl Lahir", value = "${resident.tempat_lahir}, ${resident.tanggal_lahir}")
                        DetailItem(label = "Jenis Kelamin", value = resident.jenis_kelamin)
                        DetailItem(label = "Agama", value = resident.agama)
                        DetailItem(label = "Alamat", value = resident.alamat)
                        
                        // STATUS KEPEMILIKAN IDENTITAS
                        DetailSection(title = "STATUS IDENTITAS")
                        DetailItem(label = "Wajib Identitas", value = resident.wajib_identitas)
                        DetailItem(label = "Identitas Elektronik", value = resident.identitas_elektronik)
                        DetailItem(label = "Status Rekam", value = resident.status_rekam)
                        DetailItem(label = "Tag ID Card", value = resident.tag_id_card)
                        DetailItem(label = "No KK Sebelumnya", value = resident.nomor_kk_sebelumnya)
                        DetailItem(label = "Hubungan Keluarga", value = resident.hubungan_keluarga)
                        DetailItem(label = "Status Penduduk", value = resident.status_penduduk)
                        
                        // DATA KELAHIRAN
                        DetailSection(title = "DATA KELAHIRAN")
                        DetailItem(label = "No Akta Kelahiran", value = resident.nomor_akta_kelahiran)
                        DetailItem(label = "Waktu Kelahiran", value = resident.waktu_kelahiran)
                        DetailItem(label = "Tempat Dilahirkan", value = resident.tempat_dilahirkan)
                        DetailItem(label = "Jenis Kelahiran", value = resident.jenis_kelahiran)
                        DetailItem(label = "Anak Ke", value = resident.anak_ke)
                        DetailItem(label = "Penolong Kelahiran", value = resident.penolong_kelahiran)
                        DetailItem(label = "Berat Lahir", value = "${resident.berat_lahir} kg")
                        DetailItem(label = "Panjang Lahir", value = "${resident.panjang_lahir} cm")
                        
                        // PENDIDIKAN DAN PEKERJAAN
                        DetailSection(title = "PENDIDIKAN & PEKERJAAN")
                        DetailItem(label = "Pendidikan KK", value = resident.pendidikan_kk)
                        DetailItem(label = "Pendidikan Tempuh", value = resident.pendidikan_tempuh)
                        DetailItem(label = "Pekerjaan", value = resident.pekerjaan)
                        
                        // DATA KEWARGANEGARAAN
                        DetailSection(title = "KEWARGANEGARAAN")
                        DetailItem(label = "Suku/Etnis", value = resident.suku_etnis)
                        DetailItem(label = "Status WN", value = resident.status_warga_negara)
                        DetailItem(label = "No Paspor", value = resident.nomor_paspor)
                        DetailItem(label = "Tgl Berakhir Paspor", value = resident.tgl_berakhir_paspor)
                        
                        // DATA ORANG TUA
                        DetailSection(title = "DATA ORANG TUA")
                        DetailItem(label = "NIK Ayah", value = resident.nik_ayah)
                        DetailItem(label = "Nama Ayah", value = resident.nama_ayah)
                        DetailItem(label = "NIK Ibu", value = resident.nik_ibu)
                        DetailItem(label = "Nama Ibu", value = resident.nama_ibu)
                        
                        // ALAMAT DETAIL
                        DetailSection(title = "DETAIL ALAMAT")
                        DetailItem(label = "Dusun", value = resident.dusun)
                        DetailItem(label = "RW/RT", value = "RW ${resident.rw} / RT ${resident.rt}")
                        DetailItem(label = "Alamat Sebelumnya", value = resident.alamat_sebelumnya)
                        DetailItem(label = "No Telepon", value = resident.no_telepon)
                        DetailItem(label = "Email", value = resident.email)
                        DetailItem(label = "Telegram", value = resident.telegram)
                        DetailItem(label = "Cara Hubungi", value = resident.cara_hubung)
                        
                        // STATUS PERKAWINAN
                        DetailSection(title = "STATUS PERKAWINAN")
                        DetailItem(label = "Status", value = resident.status_perkawinan)
                        DetailItem(label = "No Akta Nikah", value = resident.no_akta_nikah)
                        DetailItem(label = "Tgl Perkawinan", value = resident.tanggal_perkawinan)
                        DetailItem(label = "Akta Perceraian", value = resident.akta_perceraian)
                        DetailItem(label = "Tgl Perceraian", value = resident.tanggal_perceraian)
                        
                        // DATA KESEHATAN
                        DetailSection(title = "KESEHATAN")
                        DetailItem(label = "Gol Darah", value = resident.golongan_darah)
                        DetailItem(label = "Cacat", value = resident.cacat)
                        DetailItem(label = "Sakit Menahun", value = resident.sakit_menahun)
                        DetailItem(label = "Akseptor KB", value = resident.akseptor_kb)
                        DetailItem(label = "Asuransi", value = resident.asuransi_kesehatan)
                        DetailItem(label = "No BPJS Ketenagakerjaan", value = resident.no_bpjs_ketenagakerjaan)
                        
                        // LAINNYA
                        DetailSection(title = "LAINNYA")
                        DetailItem(label = "Baca Huruf", value = resident.dapat_membaca_huruf)
                        DetailItem(label = "Keterangan", value = resident.keterangan)
                        
                        if (resident.status == "PENDING") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showRejectDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                                    Text("REJECT")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                                    Text("APPROVE")
                                }
                            }
                        } else if (resident.status == "REJECTED" && !resident.reject_reason.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Alasan Tolak: ${resident.reject_reason}", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Submission") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (rejectReason.isNotBlank()) {
                        onReject(rejectReason)
                        showRejectDialog = false
                    }
                }) { Text("Confirm Reject") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DetailSection(title: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF00838F),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.Gray)
        Text(value.ifEmpty { "-" }, modifier = Modifier.weight(1.5f), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
fun FilterTabs(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("ALL", "PENDING", "APPROVED", "REJECTED")
    
    ScrollableTabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        containerColor = Color.White,
        contentColor = Color(0xFF00BCD4),
        edgePadding = 16.dp,
        divider = {}
    ) {
        filters.forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                text = { Text(filter, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tidak ada data warga", color = Color.Gray)
    }
}
