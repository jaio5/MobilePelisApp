package com.example.pppp.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AdminModerationScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Moderación - Admin")
        Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Volver")
        }
        // Aquí puedes añadir botones para navegar a otras pantallas de moderación
    }
}

