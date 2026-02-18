package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorScreen(
    message: String = "Ha ocurrido un error inesperado.",
    onBack: (() -> Unit)? = null
) {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                onBack?.let {
                    Button(onClick = it) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

