package com.kkn.situntang.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isAdminMode by mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun onLoginClicked() {
        if (isAdminMode) {
            loginAsAdmin()
        }
    }

    private fun loginAsAdmin() {
        if (username.isBlank()) {
            _loginState.value = LoginState.Error("Username tidak boleh kosong")
            return
        }
        if (password.length < 6) {
            _loginState.value = LoginState.Error("Password minimal 6 karakter")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Gunakan format email resmi admin: admin@test.com
                // Kita asumsikan username yang diinput adalah 'admin'
                val email = if (username.lowercase().trim() == "admin") "admin@test.com" else "${username.lowercase().trim()}@test.com"
                
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = LoginState.Success("ADMIN")
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("provider is disabled")) {
                    _loginState.value = LoginState.Error("ERROR: Aktifkan 'Email/Password' di Firebase Console")
                } else if (username.lowercase().trim() == "admin" && password == "admin123") {
                    // Jika belum ada, coba buat akun admin@test.com untuk pertama kali
                    try {
                        auth.createUserWithEmailAndPassword("admin@test.com", "admin123").await()
                        _loginState.value = LoginState.Success("ADMIN")
                    } catch (createEx: Exception) {
                        _loginState.value = LoginState.Error("Username atau Password salah")
                    }
                } else {
                    _loginState.value = LoginState.Error("Username atau Password salah")
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginState.value = LoginState.Success("WARGA")
                    } else {
                        _loginState.value = LoginState.Error(task.exception?.message ?: "Login Google Gagal")
                    }
                }
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
