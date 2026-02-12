package com.kkn.situntang

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kkn.situntang.ui.FormState
import com.kkn.situntang.ui.ResidentFormScreen
import com.kkn.situntang.ui.ResidentViewModel
import com.kkn.situntang.ui.admin.AdminDashboard
import com.kkn.situntang.ui.admin.AdminResidentListScreen
import com.kkn.situntang.ui.admin.AdminSuratListScreen
import com.kkn.situntang.ui.admin.AdminSuratDetailScreen
import com.kkn.situntang.ui.admin.AdminSettingsScreen
import com.kkn.situntang.ui.auth.LoginScreen
import com.kkn.situntang.ui.home.HomeScreen
import com.kkn.situntang.ui.theme.SiTuntangTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SiTuntangTheme {
                var currentScreen by remember { mutableStateOf<String>("loading") }
                var userRole by remember { mutableStateOf<String>("WARGA") }
                var selectedSuratId by remember { mutableStateOf("") }
                
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val context = LocalContext.current
                val residentViewModel: ResidentViewModel = viewModel()

                // State untuk handle double back to exit
                var lastBackPressTime by remember { mutableLongStateOf(0L) }

                // 1. Auth State Listener: Otomatis ke login jika terdeteksi logout (misal: setelah ganti password)
                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        if (firebaseAuth.currentUser == null && currentScreen != "login" && currentScreen != "loading") {
                            currentScreen = "login"
                        }
                    }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                LaunchedEffect(Unit) {
                    val user = auth.currentUser
                    if (user != null) {
                        try {
                            val adminDoc = firestore.collection("admins").document(user.uid).get().await()
                            if (adminDoc.exists()) {
                                userRole = "ADMIN"
                                currentScreen = "admin_dashboard"
                            } else {
                                userRole = "WARGA"
                                currentScreen = "home"
                            }
                            residentViewModel.refreshUser()
                        } catch (e: Exception) {
                            userRole = "WARGA"
                            currentScreen = "home"
                            residentViewModel.refreshUser()
                        }
                    } else {
                        currentScreen = "login"
                    }
                }

                val formState by residentViewModel.formState.collectAsState()
                LaunchedEffect(formState) {
                    when (formState) {
                        is FormState.Success -> {
                            Toast.makeText(context, "Pendataan tersimpan!", Toast.LENGTH_SHORT).show()
                            currentScreen = "home"
                            residentViewModel.resetForm()
                        }
                        is FormState.Error -> {
                            Toast.makeText(context, (formState as FormState.Error).message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val duration = 500
                        val easing = FastOutSlowInEasing
                        
                        if (targetState == "login") {
                            fadeIn(animationSpec = tween(duration, easing = easing)) togetherWith
                            fadeOut(animationSpec = tween(duration, easing = easing)) + slideOutHorizontally { it }
                        } else {
                            (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it / 4 } + 
                             fadeIn(animationSpec = tween(duration, easing = easing))) togetherWith
                            (slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it / 4 } + 
                             fadeOut(animationSpec = tween(duration, easing = easing)))
                        }
                    },
                    label = "MainScreenTransition"
                ) { screen ->
                    when (screen) {
                        "login" -> {
                            LoginScreen(
                                onNavigateToHome = { role ->
                                    if (role == "ADMIN") {
                                        userRole = "ADMIN"
                                        currentScreen = "admin_dashboard"
                                    } else {
                                        userRole = "WARGA"
                                        currentScreen = "home"
                                    }
                                    residentViewModel.refreshUser()
                                }
                            )
                        }
                        "home" -> {
                            HomeScreen(
                                onNavigateToPendataan = { currentScreen = "pendataan" },
                                onLogout = {
                                    auth.signOut()
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken("524744030103-tlgu2k0eilmeq99g6otdsilbhmja3t2o.apps.googleusercontent.com")
                                        .requestEmail()
                                        .build()
                                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        currentScreen = "login"
                                        residentViewModel.refreshUser()
                                    }
                                },
                                viewModel = residentViewModel
                            )
                        }
                        "admin_dashboard" -> {
                            // Logic: 2x back untuk keluar aplikasi di Dashboard Admin
                            BackHandler {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastBackPressTime < 2000) {
                                    (context as? Activity)?.finish()
                                } else {
                                    lastBackPressTime = currentTime
                                    Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                                }
                            }
                            AdminDashboard(
                                onLogout = {
                                    auth.signOut()
                                    currentScreen = "login"
                                    residentViewModel.refreshUser()
                                },
                                onNavigateToPendataan = { currentScreen = "admin_resident_list" },
                                onNavigateToSurat = { currentScreen = "admin_surat_list" },
                                onNavigateToSettings = { currentScreen = "admin_settings" }
                            )
                        }
                        "admin_resident_list" -> {
                            // Logic: Back kembali ke Dashboard Admin
                            BackHandler { currentScreen = "admin_dashboard" }
                            AdminResidentListScreen(
                                onBack = { currentScreen = "admin_dashboard" }
                            )
                        }
                        "admin_surat_list" -> {
                            // Logic: Back kembali ke Dashboard Admin
                            BackHandler { currentScreen = "admin_dashboard" }
                            AdminSuratListScreen(
                                onNavigateToDetail = { id ->
                                    selectedSuratId = id
                                    currentScreen = "admin_surat_detail"
                                },
                                onBack = { currentScreen = "admin_dashboard" }
                            )
                        }
                        "admin_surat_detail" -> {
                            // Logic: Back kembali ke List Surat
                            BackHandler { currentScreen = "admin_surat_list" }
                            AdminSuratDetailScreen(
                                requestId = selectedSuratId,
                                onBack = { currentScreen = "admin_surat_list" }
                            )
                        }
                        "admin_settings" -> {
                            // Logic: Back kembali ke Dashboard Admin
                            BackHandler { currentScreen = "admin_dashboard" }
                            AdminSettingsScreen(
                                onBack = { currentScreen = "admin_dashboard" }
                            )
                        }
                        "pendataan" -> {
                            ResidentFormScreen(
                                viewModel = residentViewModel,
                                onBack = {
                                    Toast.makeText(context, "Pendataan berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                                    currentScreen = "home"
                                    residentViewModel.resetForm()
                                }
                            )
                        }
                        "loading" -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
