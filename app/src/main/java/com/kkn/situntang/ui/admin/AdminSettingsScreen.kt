package com.kkn.situntang.ui.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val focusManager = LocalFocusManager.current

    var showForm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var oldPasswordVisible by remember { mutableStateOf(false) }

    val handleSave = {
        if (oldPasswordInput.isBlank() || newPasswordInput.length < 6) {
            Toast.makeText(context, "Harap masukkan password lama & password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
        } else {
            scope.launch {
                isLoading = true
                try {
                    val user = auth.currentUser
                    if (user != null) {
                        // Gunakan email tetap admin@test.com untuk verifikasi
                        val email = "admin@test.com"
                        val credential = EmailAuthProvider.getCredential(email, oldPasswordInput)
                        
                        try {
                            user.reauthenticate(credential).await()
                        } catch (e: Exception) {
                            throw Exception("Verifikasi gagal: Password lama salah")
                        }

                        // Update Password di Firebase Auth
                        user.updatePassword(newPasswordInput).await()

                        // Update Firestore untuk backup data (opsional)
                        firestore.collection("settings").document("admin_auth")
                            .set(mapOf(
                                "email" to "admin@test.com", 
                                "password" to newPasswordInput,
                                "updatedAt" to com.google.firebase.Timestamp.now()
                            )).await()

                        Toast.makeText(context, "Berhasil! Silakan login ulang dengan password baru.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        onBack()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message ?: "Terjadi kesalahan sistem", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Keamanan Admin") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showForm = !showForm },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ubah Password Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Update password login akun admin@test.com", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(if (showForm) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }

            AnimatedVisibility(visible = showForm) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Verifikasi Password Saat Ini", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        OutlinedTextField(
                            value = oldPasswordInput,
                            onValueChange = { oldPasswordInput = it },
                            label = { Text("Password Lama") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                    Icon(if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Password Baru", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text("Password Baru (Min. 6 Karakter)") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { 
                                focusManager.clearFocus()
                                handleSave()
                            })
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { 
                                focusManager.clearFocus()
                                handleSave() 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            else Text("SIMPAN PASSWORD & LOGOUT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
