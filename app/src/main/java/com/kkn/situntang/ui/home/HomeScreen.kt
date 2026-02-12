package com.kkn.situntang.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.kkn.situntang.R
import com.kkn.situntang.model.News
import com.kkn.situntang.model.Resident
import com.kkn.situntang.ui.ResidentViewModel
import com.kkn.situntang.ui.surat.SuratFormScreen
import com.kkn.situntang.ui.surat.SuratListScreen

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

object HomeNavItem : BottomNavItem("home_content", "Home", Icons.Outlined.Home, Icons.Filled.Home)
object ProfileNavItem : BottomNavItem("profile_content", "Profile", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)

@Composable
fun HomeScreen(
    onNavigateToPendataan: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ResidentViewModel = viewModel(),
    newsViewModel: NewsViewModel = viewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf(HomeNavItem, ProfileNavItem)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(if (currentRoute == item.route) item.selectedIcon else item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeNavItem.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(HomeNavItem.route) {
                HomeContent(
                    onNavigateToPendataan = { navController.navigate("riwayat_pendataan") }, 
                    onNavigateToSurat = { navController.navigate("surat_list") },
                    onNavigateToAllNews = { navController.navigate("all_news") },
                    viewModel = viewModel,
                    newsViewModel = newsViewModel
                )
            }
            composable(ProfileNavItem.route) {
                var showLogoutDialog by remember { mutableStateOf(false) }
                
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Konfirmasi Logout") },
                        text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    onLogout()
                                }
                            ) { Text("Ya, Logout") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                ProfileContent(
                    onLogout = { showLogoutDialog = true },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAbout = { navController.navigate("about") },
                    viewModel = viewModel
                )
            }
            composable("settings") {
                SettingsContent(onBack = { navController.popBackStack() })
            }
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable("riwayat_pendataan") {
                RiwayatPendataanScreen(
                    viewModel = viewModel,
                    onNavigateToForm = onNavigateToPendataan,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("surat_list") {
                SuratListScreen(
                    onNavigateToForm = { navController.navigate("surat_form") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("surat_form") {
                SuratFormScreen(onBack = { navController.popBackStack() })
            }
            composable("all_news") {
                AllNewsScreen(newsViewModel = newsViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Tentang Aplikasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_aplikasi),
                        contentDescription = "Logo Aplikasi",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "SiTuntang", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Bold
            )
            Text(
                "Versi 1.0.0", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Dibuat dengan ❤️ oleh mahasiswa KKN UNNES GIAT 15",
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "© 2026 Desa Tuntang. All rights reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun HomeContent(
    onNavigateToPendataan: () -> Unit, 
    onNavigateToSurat: () -> Unit, 
    onNavigateToAllNews: () -> Unit,
    viewModel: ResidentViewModel,
    newsViewModel: NewsViewModel
) {
    val user = FirebaseAuth.getInstance().currentUser
    val newsList by newsViewModel.newsList.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Halo,", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        user?.displayName ?: "Warga Desa",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Selamat Datang di SiTuntang",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        item {
            Text(
                "Menu Utama",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuGridItem(
                    title = "Pendataan",
                    icon = Icons.Default.People,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPendataan
                )
                MenuGridItem(
                    title = "Surat",
                    icon = Icons.Default.Description,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSurat
                )
                MenuGridItem(
                    title = "Informasi",
                    icon = Icons.Default.Info,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tuntang-desa.web.id/"))
                        context.startActivity(intent)
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Berita Desa Terbaru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToAllNews) {
                    Text("Lihat Semua")
                }
            }
        }

        // Display only top 5 news
        items(newsList.take(5)) { news ->
            NewsCard(
                news = news,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                    context.startActivity(intent)
                }
            )
        }
        
        if (newsList.size > 5) {
            item {
                OutlinedButton(
                    onClick = onNavigateToAllNews,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Baca Berita Lainnya")
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AllNewsScreen(newsViewModel: NewsViewModel, onBack: () -> Unit) {
    val newsList by newsViewModel.newsList.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Arsip Berita Desa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newsList) { news ->
                NewsCard(
                    news = news,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun RiwayatPendataanScreen(
    viewModel: ResidentViewModel,
    onNavigateToForm: () -> Unit,
    onBack: () -> Unit
) {
    val mySubmissions by viewModel.mySubmissions.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Riwayat Pendataan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToForm,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Data")
            }
        }
    ) { padding ->
        if (mySubmissions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mySubmissions) { resident ->
                    ResidentRiwayatItem(resident = resident)
                }
            }
        } else {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada riwayat pendataan", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun MenuGridItem(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = contentColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NewsCard(news: News, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.logo_desa),
                placeholder = painterResource(id = R.drawable.logo_desa)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(news.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(
                    news.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Baca Selengkapnya", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ResidentRiwayatItem(resident: Resident) {
    val statusColor = when (resident.status) {
        "PENDING" -> MaterialTheme.colorScheme.primary
        "APPROVED" -> Color(0xFF388E3C)
        "REJECTED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(resident.nama, fontWeight = FontWeight.Bold) },
            supportingContent = { 
                Column {
                    Text("NIK: ${resident.nik}")
                    Text(resident.created_at?.toDate()?.toString()?.substring(0, 10) ?: "-")
                }
            },
            trailingContent = {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        resident.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun ProfileContent(onLogout: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToAbout: () -> Unit, viewModel: ResidentViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    var profileImageUrl by remember { mutableStateOf(user?.photoUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    if (profileImageUrl != null) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp).fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    user?.displayName ?: "Warga", 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    user?.email ?: "Belum ada email", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            ProfileMenuItem(title = "Settings", icon = Icons.Default.Settings, onClick = onNavigateToSettings)
            ProfileMenuItem(title = "Tentang Aplikasi", icon = Icons.Outlined.Info, onClick = onNavigateToAbout)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ProfileMenuItem(title = "Logout", icon = Icons.AutoMirrored.Filled.Logout, isDanger = true, onClick = onLogout)
        }
    }
}

@Composable
fun SettingsContent(onBack: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var profileImageUrl by remember { mutableStateOf(user?.photoUrl) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUrl = it
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(it)
                .build()
            user?.updateProfile(profileUpdates)
            Toast.makeText(context, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            ProfileMenuItem(title = "Ganti Foto Profil", icon = Icons.Default.PhotoCamera) {
                galleryLauncher.launch("image/*")
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
