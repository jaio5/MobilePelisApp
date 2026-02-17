package com.example.pppp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pppp.uiState.AdminUiState
import com.example.pppp.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    token: String,
    currentUserId: Long
) {
    val uiState by viewModel.uiState.collectAsState()
    var editUserId by remember { mutableStateOf<Long?>(null) }
    var editUsername by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userIdToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers(token)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.UserUpdated,
            is AdminUiState.UserDeleted -> {
                editUserId = null
                viewModel.loadUsers(token)
            }
            else -> {}
        }
    }

    // Definición local de StatItem para evitar error de referencia
    @Composable
    fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Panel de Admin",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gestión de usuarios",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner de admin
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE94560).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFE94560),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Modo Administrador",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560)
                    )
                }
            }

            when (uiState) {
                is AdminUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is AdminUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            (uiState as AdminUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is AdminUiState.Success -> {
                    val users = (uiState as AdminUiState.Success).users

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(
                                value = users.size.toString(),
                                label = "Usuarios totales",
                                icon = Icons.Filled.Person
                            )
                            StatItem(
                                value = users.count { it.roles.contains("ROLE_ADMIN") }.toString(),
                                label = "Administradores",
                                icon = Icons.Filled.Settings
                            )
                        }
                    }

                    // Lista de usuarios
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(users) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Avatar
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            color = if (user.roles.contains("ROLE_ADMIN"))
                                                Color(0xFFE94560).copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    user.username.take(2).uppercase(),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = if (user.roles.contains("ROLE_ADMIN"))
                                                        Color(0xFFE94560)
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            if (editUserId == user.id) {
                                                OutlinedTextField(
                                                    value = editUsername,
                                                    onValueChange = { editUsername = it },
                                                    label = { Text("Nombre de usuario") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Text(
                                                    user.username,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                user.email?.let {
                                                    Text(
                                                        it,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                                if (user.roles.contains("ROLE_ADMIN")) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFE94560).copy(alpha = 0.2f),
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        Text(
                                                            "ADMIN",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFE94560),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Acciones
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (editUserId == user.id) {
                                            FilledTonalButton(
                                                onClick = {
                                                    if (editUsername.isNotBlank()) {
                                                        viewModel.updateUser(token, user.copy(username = editUsername))
                                                        editUserId = null // Reset tras guardar
                                                        editUsername = ""
                                                    }
                                                },
                                                enabled = editUsername.isNotBlank(),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Guardar")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    editUserId = null
                                                    editUsername = ""
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.Close, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Cancelar")
                                            }
                                        } else {
                                            FilledTonalButton(
                                                onClick = {
                                                    editUserId = user.id
                                                    editUsername = user.username
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Editar")
                                            }
                                            Button(
                                                onClick = {
                                                    showDeleteDialog = true
                                                    userIdToDelete = user.id
                                                },
                                                enabled = user.id != currentUserId,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFE94560),
                                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.Delete, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Eliminar")
                                            }
                                        }
                                    }

                                    if (user.id == currentUserId) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No puedes eliminarte a ti mismo",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                is AdminUiState.SelfDeleteError -> {
                    Text(
                        "No puedes eliminarte a ti mismo",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                else -> {}
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                userIdToDelete = null
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este usuario? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        userIdToDelete?.let { id ->
                            viewModel.deleteUser(token, id, currentUserId)
                        }
                        showDeleteDialog = false
                        userIdToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    userIdToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
