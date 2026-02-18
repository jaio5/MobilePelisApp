package com.example.pppp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pppp.data.remote.dataclass.User
import com.example.pppp.uiState.AdminUiState
import com.example.pppp.viewmodel.AdminViewModel

private val AdminRed = Color(0xFFE94560)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    token: String,
    currentUserId: Long,
    onBack: () -> Unit = {},
    onNavigateModeration: () -> Unit = {},
    onNavigateMovies: () -> Unit = {},
    onNavigateReviews: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateMovieDetail: (Long) -> Unit = {},
    onNavigateUserProfile: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<User?>(null) }
    val showDeleteDialog = deleteTarget != null

    // Log para diagnóstico
    LaunchedEffect(uiState) {
        println("[AdminScreen] uiState: $uiState")
    }

    LaunchedEffect(Unit) {
        viewModel.loadUsers(token)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.UserUpdated,
            is AdminUiState.UserDeleted -> {
                deleteTarget = null
                viewModel.loadUsers(token)
            }
            else -> {}
        }
    }

    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = AdminRed, modifier = Modifier.size(32.dp))
            },
            title = { Text("Eliminar usuario", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Seguro que quieres eliminar a \"${deleteTarget!!.username}\"?\nEsta acción no se puede deshacer.", textAlign = TextAlign.Center)
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteUser(token, deleteTarget!!.id, currentUserId)
                    deleteTarget = null
                }, colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                title = {
                    Column {
                        Text("Panel de Administración", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Gestión de usuarios", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers(token) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Barra de navegación para admin
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onNavigateModeration) {
                    Icon(Icons.Filled.Gavel, contentDescription = "Moderación")
                    Spacer(Modifier.width(4.dp))
                    Text("Moderación")
                }
                Button(onClick = onNavigateMovies) {
                    Icon(Icons.Filled.Movie, contentDescription = "Películas")
                    Spacer(Modifier.width(4.dp))
                    Text("Películas")
                }
                Button(onClick = onNavigateReviews) {
                    Icon(Icons.Filled.RateReview, contentDescription = "Reviews")
                    Spacer(Modifier.width(4.dp))
                    Text("Reviews")
                }
                Button(onClick = onNavigateHome) {
                    Icon(Icons.Filled.Home, contentDescription = "Home")
                }
                Button(onClick = onNavigateProfile) {
                    Icon(Icons.Filled.Person, contentDescription = "Perfil")
                }
                Button(onClick = onNavigateSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is AdminUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AdminRed)
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando usuarios...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            is AdminUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { viewModel.loadUsers(token) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }
            is AdminUiState.Success -> {
                val users = state.users
                val totalAdmins = users.count { it.roles.contains("ROLE_ADMIN") }
                val totalUsers = users.count { !it.roles.contains("ROLE_ADMIN") }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Admin badge banner
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AdminRed.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = AdminRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Modo Administrador activo", fontWeight = FontWeight.SemiBold, color = AdminRed, fontSize = 14.sp)
                            }
                        }
                    }

                    // Stats row
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Group, value = users.size.toString(), label = "Total usuarios")
                            AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.AdminPanelSettings, value = totalAdmins.toString(), label = "Admins", iconTint = AdminRed)
                            AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Person, value = totalUsers.toString(), label = "Usuarios")
                        }
                    }

                    // Section header
                    item {
                        Text("Lista de usuarios", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // User cards
                    items(users, key = { it.id }) { user ->
                        val isCurrentUser = user.id == currentUserId
                        val isAdmin = user.roles.contains("ROLE_ADMIN")
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                // User info row
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    // Avatar circle
                                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = if (isAdmin) AdminRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(user.username.take(2).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isAdmin) AdminRed else MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { onNavigateUserProfile(user.id) })
                                            if (isCurrentUser) {
                                                Spacer(Modifier.width(6.dp))
                                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                                                    Text("TÚ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                        user.email?.let {
                                            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                        }
                                        if (isAdmin) {
                                            Spacer(Modifier.height(4.dp))
                                            Surface(shape = RoundedCornerShape(4.dp), color = AdminRed.copy(alpha = 0.15f)) {
                                                Text("ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AdminRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(12.dp))

                                // Action buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { deleteTarget = user }, enabled = !isCurrentUser, colors = ButtonDefaults.buttonColors(containerColor = AdminRed, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Eliminar", color = if (isCurrentUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else Color.White)
                                    }
                                    Button(onClick = { onNavigateMovieDetail(user.id) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Movie, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ver Película")
                                    }
                                    Button(onClick = { onNavigateProfile() }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Person, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Perfil")
                                    }
                                    Button(onClick = { onNavigateSettings() }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Settings, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ajustes")
                                    }
                                }
                                if (isCurrentUser) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("No puedes eliminarte a ti mismo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
            is AdminUiState.SelfDeleteError -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No puedes eliminarte a ti mismo", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun AdminStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color = Color.Unspecified
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (iconTint == Color.Unspecified) MaterialTheme.colorScheme.primary else iconTint
            )
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}