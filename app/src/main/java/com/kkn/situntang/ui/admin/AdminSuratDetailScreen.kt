package com.kkn.situntang.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.situntang.ui.surat.StatusBadge
import com.kkn.situntang.ui.surat.SuratState
import com.kkn.situntang.ui.surat.SuratViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSuratDetailScreen(
    requestId: String,
    viewModel: SuratViewModel = viewModel(),
    onBack: () -> Unit
) {
    val allRequests by viewModel.allRequests.collectAsState()
    val suratState by viewModel.suratState.collectAsState()
    val request = allRequests.find { it.id == requestId }
    val context = LocalContext.current
    
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(suratState) {
        if (suratState is SuratState.Success) {
            Toast.makeText(context, (suratState as SuratState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBack()
        } else if (suratState is SuratState.Error) {
            Toast.makeText(context, (suratState as SuratState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pengajuan Surat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Data tidak ditemukan")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status Saat Ini", style = MaterialTheme.typography.labelLarge)
                            StatusBadge(status = request.status)
                        }
                    }
                }

                InfoSection(label = "NIK", value = request.nik)
                InfoSection(label = "Nama Lengkap", value = request.nama)
                InfoSection(label = "Tanggal Lahir", value = request.tanggalLahir)
                InfoSection(label = "Alamat", value = request.alamat)
                InfoSection(label = "Jenis Surat", value = request.jenisSurat)
                InfoSection(label = "Keperluan", value = request.keperluan)
                InfoSection(label = "Metode Pengambilan", value = if(request.metodePengambilan == "ANTAR") "Diantar ke rumah" else "Ambil sendiri ke kantor desa")
                
                request.catatan?.let { 
                    InfoSection(label = "Catatan Warga", value = it) 
                }

                if (request.status == "DITOLAK" && !request.rejectReason.isNullOrEmpty()) {
                    InfoSection(label = "Alasan Penolakan", value = request.rejectReason, isError = true)
                }

                if (request.status == "PENDING") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.approveRequest(requestId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = suratState !is SuratState.Loading
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("TERIMA")
                        }
                        
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            enabled = suratState !is SuratState.Loading
                        ) {
                            Icon(Icons.Default.Close, null)
                            Spacer(Modifier.width(8.dp))
                            Text("TOLAK")
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Tolak Pengajuan") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Alasan Penolakan (Wajib)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            viewModel.rejectRequest(requestId, rejectReason)
                            showRejectDialog = false
                        } else {
                            Toast.makeText(context, "Alasan wajib diisi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Konfirmasi") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun InfoSection(label: String, value: String, isError: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label, 
            style = MaterialTheme.typography.labelMedium, 
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Text(
            value, 
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}
