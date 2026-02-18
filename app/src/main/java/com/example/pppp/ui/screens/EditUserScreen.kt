package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditUserScreen(
    userId: Long,
    onBack: () -> Unit
) {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Editar usuario (ID: $userId) (por implementar)")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Volver")
                }
            }
        }
    }
}

