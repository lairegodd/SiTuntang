package com.kkn.situntang.ui.admin

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.situntang.model.SuratRequest
import com.kkn.situntang.ui.surat.StatusBadge
import com.kkn.situntang.ui.surat.SuratViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AdminSuratListScreen(
    viewModel: SuratViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val allRequests by viewModel.allRequests.collectAsState()
    
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllRequests()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Pengajuan") },
            text = { Text("Apakah Anda yakin ingin menghapus ${selectedIds.size} data pengajuan ini? Data yang dihapus tidak dapat dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRequests(selectedIds.toList())
                        isSelectionMode = false
                        selectedIds.clear()
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
                    title = { Text("${selectedIds.size} Terpilih") },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSelectionMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Batal")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (selectedIds.size == allRequests.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(allRequests.map { it.id })
                            }
                        }) {
                            Text(if (selectedIds.size == allRequests.size) "Batal Semua" else "Pilih Semua")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Daftar Pengajuan Surat") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (allRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Tidak ada pengajuan surat masuk.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allRequests) { request ->
                    val isSelected = selectedIds.contains(request.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { 
                                    if (isSelectionMode) {
                                        if (isSelected) {
                                            selectedIds.remove(request.id)
                                            if (selectedIds.isEmpty()) isSelectionMode = false
                                        } else {
                                            selectedIds.add(request.id)
                                        }
                                    } else {
                                        onNavigateToDetail(request.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedIds.add(request.id)
                                    }
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null // Handled by Card onClick
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(request.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    StatusBadge(status = request.status)
                                }
                                Text(request.jenisSurat, style = MaterialTheme.typography.bodyLarge)
                                Text(request.tanggalPengajuan, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}
