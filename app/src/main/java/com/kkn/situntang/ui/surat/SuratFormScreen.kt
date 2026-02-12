package com.kkn.situntang.ui.surat

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuratFormScreen(
    viewModel: SuratViewModel = viewModel(),
    onBack: () -> Unit
) {
    val suratState by viewModel.suratState.collectAsState()
    val context = LocalContext.current
    
    var nik by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var jenisSurat by remember { mutableStateOf("Pilih Jenis Surat") }
    var keperluan by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var metodePengambilan by remember { mutableStateOf("AMBIL_SENDIRI") } // ANTAR / AMBIL_SENDIRI

    var jenisExpanded by remember { mutableStateOf(false) }
    
    val jenisSuratList = listOf(
        "Surat Cuti",
        "Surat Keterangan Usaha",
        "Surat Domisili",
        "Surat Keterangan Tidak Mampu",
        "Lainnya"
    )

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
                title = { Text("Form Pengajuan Surat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Isi data di bawah ini dengan benar untuk pengajuan surat desa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = nik,
                onValueChange = { if (it.length <= 16) nik = it },
                label = { Text("NIK (16 Digit)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Badge, null) }
            )

            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            OutlinedTextField(
                value = tanggalLahir,
                onValueChange = { tanggalLahir = it },
                label = { Text("Tanggal Lahir (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
            )

            OutlinedTextField(
                value = alamat,
                onValueChange = { alamat = it },
                label = { Text("Alamat") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Home, null) }
            )

            // Dropdown Jenis Surat
            ExposedDropdownMenuBox(
                expanded = jenisExpanded,
                onExpandedChange = { jenisExpanded = !jenisExpanded }
            ) {
                OutlinedTextField(
                    value = jenisSurat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis Surat") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jenisExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Description, null) }
                )
                ExposedDropdownMenu(
                    expanded = jenisExpanded,
                    onDismissRequest = { jenisExpanded = false }
                ) {
                    jenisSuratList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                jenisSurat = item
                                jenisExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = keperluan,
                onValueChange = { keperluan = it },
                label = { Text("Keperluan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Info, null) }
            )

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )

            Text("Metode Pengambilan Surat", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    RadioButton(
                        selected = metodePengambilan == "AMBIL_SENDIRI",
                        onClick = { metodePengambilan = "AMBIL_SENDIRI" }
                    )
                    Text("Ambil Sendiri")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    RadioButton(
                        selected = metodePengambilan == "ANTAR",
                        onClick = { metodePengambilan = "ANTAR" }
                    )
                    Text("Diantar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nik.length != 16 || nama.isBlank() || jenisSurat == "Pilih Jenis Surat" || keperluan.isBlank()) {
                        Toast.makeText(context, "Harap lengkapi semua data wajib", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitRequest(nik, nama, tanggalLahir, alamat, jenisSurat, keperluan, catatan, metodePengambilan)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = suratState !is SuratState.Loading
            ) {
                if (suratState is SuratState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Kirim Pengajuan", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
