package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BulkUploadScreen(
    onNavigateBack: () -> Unit
) {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Carga masiva de películas (por implementar)")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Volver")
                }
            }
        }
    }
}

