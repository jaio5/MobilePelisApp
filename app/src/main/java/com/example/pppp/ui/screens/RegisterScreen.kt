package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pppp.uiState.AuthUiState

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    uiState: AuthUiState,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf("") }

    fun validate(): Boolean {
        usernameError = if (username.isBlank()) "El usuario es obligatorio" else ""
        emailError = if (email.isBlank()) "El email es obligatorio" else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Email inválido" else ""
        passwordError = if (password.length < 6) "La contraseña debe tener al menos 6 caracteres" else ""
        formError = if (usernameError.isNotEmpty() || emailError.isNotEmpty() || passwordError.isNotEmpty()) "Corrige los errores antes de continuar" else ""
        return formError.isEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as AuthUiState.Error).message}",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Red
                )
            }
            else -> {}
        }
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError.isNotEmpty()
        )
        if (usernameError.isNotEmpty()) {
            Text(usernameError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError.isNotEmpty()
        )
        if (emailError.isNotEmpty()) {
            Text(emailError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError.isNotEmpty()
        )
        if (passwordError.isNotEmpty()) {
            Text(passwordError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (formError.isNotEmpty()) {
            Text(formError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = {
                if (validate()) onRegister(username, email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onBack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}