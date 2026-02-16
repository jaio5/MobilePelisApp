package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pppp.uiState.AdminUiState
import com.example.pppp.viewmodel.AdminViewModel

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    token: String,
    currentUserId: Long,
    onHome: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Panel de Administración", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            val uiState by viewModel.uiState.collectAsState()
            var editUserId by remember { mutableStateOf<Long?>(null) }
            var editUsername by remember { mutableStateOf("") }
            var editUserError by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                viewModel.loadUsers(token)
            }

            when (uiState) {
                is AdminUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is AdminUiState.Error -> {
                    Text(text = (uiState as AdminUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                is AdminUiState.Success -> {
                    val users = (uiState as AdminUiState.Success).users
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(users.size) { idx ->
                            val user = users[idx]
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (editUserId == user.id) {
                                    BasicTextField(
                                        value = editUsername,
                                        onValueChange = { editUsername = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (editUsername.isBlank()) {
                                        editUserError = "El nombre de usuario no puede estar vacío"
                                    } else {
                                        editUserError = ""
                                    }
                                    if (editUserError.isNotEmpty()) {
                                        Text(editUserError, color = MaterialTheme.colorScheme.error)
                                    }
                                    Button(onClick = {
                                        if (editUsername.isNotBlank()) {
                                            viewModel.updateUser(token, user.copy(username = editUsername))
                                            editUserId = null
                                        } else {
                                            editUserError = "El nombre de usuario no puede estar vacío"
                                        }
                                    }) { Text("Guardar") }
                                } else {
                                    Text(user.username, modifier = Modifier.weight(1f))
                                    Button(onClick = {
                                        editUserId = user.id
                                        editUsername = user.username
                                    }) { Text("Editar") }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.deleteUser(token, user.id, currentUserId) },
                                    enabled = user.id != currentUserId
                                ) { Text("Eliminar") }
                            }
                        }
                    }
                }
                is AdminUiState.UserUpdated -> {
                    // Recargar usuarios tras editar
                    viewModel.loadUsers(token)
                    Text("Usuario actualizado correctamente")
                }
                is AdminUiState.UserDeleted -> {
                    // Recargar usuarios tras eliminar
                    viewModel.loadUsers(token)
                    Text("Usuario eliminado correctamente")
                }
                is AdminUiState.SelfDeleteError -> {
                    Text("No puedes eliminarte a ti mismo")
                }
                else -> {}
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onHome) { Text("Ir a Home") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onProfile) { Text("Perfil") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSettings) { Text("Ajustes") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}