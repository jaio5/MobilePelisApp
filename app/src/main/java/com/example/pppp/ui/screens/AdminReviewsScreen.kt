package com.example.pppp.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun AdminReviewsScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Reviews (Admin)")
        Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Volver")
        }
        // Aquí puedes añadir botones para moderar reviews, ver estadísticas, etc.
    }
}

