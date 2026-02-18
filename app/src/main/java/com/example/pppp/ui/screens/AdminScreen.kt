package com.example.pppp.ui.screens

import android.util.Log
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
    viewModel: AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    token: String,
    currentUserId: Long,
    currentUserRoles: List<String>,
    onBack: () -> Unit = {},
    onNavigateToModeration: () -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToUserProfile: (Long) -> Unit = {},
    onNavigateBulkUpload: () -> Unit = {},
    onNavigateReviews: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateMovieDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Log the current UI state for debugging
    LaunchedEffect(uiState) {
        Log.i("[AdminScreen]", "uiState: $uiState")
    }
    LaunchedEffect(uiState, currentUserRoles) {
        Log.i("[AdminScreen]", "uiState: $uiState | currentUserRoles: $currentUserRoles")
    }
    var deleteTarget by remember { mutableStateOf<User?>(null) }
    var banTarget by remember { mutableStateOf<User?>(null) }
    var deleteMovieTarget by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("email") } // "email" o "username"

    val showDeleteDialog = deleteTarget != null
    val showBanDialog = banTarget != null
    val showDeleteMovieDialog = deleteMovieTarget != null

    LaunchedEffect(uiState) {
        println("[AdminScreen] uiState: $uiState")
        // Forzar uso de variables para evitar warnings
        if (deleteTarget != null) println("[AdminScreen] deleteTarget: ${deleteTarget!!.username}")
        if (banTarget != null) println("[AdminScreen] banTarget: ${banTarget!!.username}")
        if (deleteMovieTarget != null) println("[AdminScreen] deleteMovieTarget: $deleteMovieTarget")
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers(token, currentUserRoles)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.UserUpdated,
            is AdminUiState.UserDeleted -> {
                deleteTarget = null
                banTarget = null
                viewModel.loadAllUsers(token, currentUserRoles)
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

    if (showBanDialog && banTarget != null) {
        AlertDialog(
            onDismissRequest = { banTarget = null },
            icon = {
                Icon(Icons.Filled.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
            },
            title = { Text("Banear usuario", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Seguro que quieres banear a \"${banTarget!!.username}\"?\nEl usuario no podrá acceder a la app.", textAlign = TextAlign.Center)
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.banUser(token, banTarget!!.id)
                    banTarget = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Banear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { banTarget = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteMovieDialog && deleteMovieTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteMovieTarget = null },
            icon = {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
            },
            title = { Text("Eliminar película", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Seguro que quieres eliminar la película con ID ${deleteMovieTarget}?\nEsta acción no se puede deshacer.", textAlign = TextAlign.Center)
            },
            confirmButton = {
                Button(onClick = {
                    // Aquí se debería llamar a la función de eliminar película del ViewModel si existe
                    // Ejemplo: viewModel.deleteMovie(token, deleteMovieTarget!!)
                    deleteMovieTarget = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteMovieTarget = null }) {
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
                        Text("Gestión de usuarios y películas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllUsers(token, currentUserRoles) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = onNavigateBulkUpload) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Carga masiva")
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
                Button(onClick = onNavigateToModeration) {
                    Icon(Icons.Filled.Gavel, contentDescription = "Moderación")
                    Spacer(Modifier.width(4.dp))
                    Text("Moderación")
                }
                Button(onClick = onNavigateToMovies) {
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
                Button(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar usuario") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                // Puedes implementar el menú si quieres cambiar entre email/username
                Button(onClick = {
                    if (searchType == "email") {
                        viewModel.searchUserByEmail(token, searchQuery)
                    } else {
                        viewModel.searchUserByUsername(token, searchQuery)
                    }
                }, enabled = searchQuery.isNotBlank()) {
                    Text("Buscar")
                }
            }

            // Mostrar mensajes de error detallados en la UI
            when (uiState) {
                is AdminUiState.Error -> {
                    val errorState = uiState as AdminUiState.Error
                    val errorMessage = errorState.message
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar usuarios",
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (errorMessage.isNotBlank()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                is AdminUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AdminRed)
                            Spacer(Modifier.height(16.dp))
                            Text("Buscando usuario...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
                is AdminUiState.Success -> {
                    // Mostrar los usuarios encontrados
                    val users = (uiState as AdminUiState.Success).users
                    if (users.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron usuarios.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    } else {
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
                                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.AdminPanelSettings, value = users.count { it.roles.contains("ROLE_ADMIN") }.toString(), label = "Admins", iconTint = AdminRed)
                                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Person, value = users.count { !it.roles.contains("ROLE_ADMIN") }.toString(), label = "Usuarios")
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
                                                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { onNavigateToUserProfile(user.id) })
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
                                            OutlinedButton(onClick = { banTarget = user }, enabled = !isCurrentUser, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.Block, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Banear", color = if (isCurrentUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else Color.White)
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
                                            Button(onClick = { onNavigateToSettings() }, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.Settings, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Ajustes")
                                            }
                                        }
                                        if (isCurrentUser) {
                                            Spacer(Modifier.height(8.dp))
                                            Text("No puedes eliminarte ni banearte a ti mismo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(32.dp)) }

                            // Películas admin
                            item {
                                Text("Gestión de películas", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onNavigateBulkUpload, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.FileUpload, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Carga masiva")
                                    }
                                    Button(onClick = { deleteMovieTarget = 0L }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Eliminar película")
                                    }
                                }
                            }
                        }
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