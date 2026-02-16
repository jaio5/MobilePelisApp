package com.example.pppp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pppp.uiState.AuthUiState

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    uiState: AuthUiState
) {
    var username by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf("") }

    fun validate(): Boolean {
        usernameError = if (username.isBlank()) "El usuario es obligatorio" else ""
        passwordError = if (password.length < 6) "La contraseña debe tener al menos 6 caracteres" else ""
        formError = if (usernameError.isNotEmpty() || passwordError.isNotEmpty()) "Corrige los errores antes de continuar" else ""
        return formError.isEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        when(uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as AuthUiState.Error).message}",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    color = Color.Red
                )
            }
            else -> {}
        }
        TextField(
            value = username,
            onValueChange = { username = it},
            label = { Text("Username")},
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError.isNotEmpty()
        )
        if (usernameError.isNotEmpty()) {
            Text(usernameError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Password")},
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError.isNotEmpty()
        )
        if (passwordError.isNotEmpty()) {
            Text(passwordError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (formError.isNotEmpty()) {
            Text(formError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { if (validate()) onLogin(username, password) }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No tienes una cuenta?  Regístrate",
            modifier = Modifier.clickable { onRegister() }
        )
    }
}