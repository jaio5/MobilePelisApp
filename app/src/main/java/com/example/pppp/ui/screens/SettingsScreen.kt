package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pppp.data.datastore.PreferencesDataStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onHome: () -> Unit = {},
    onProfile: () -> Unit = {},
    onAdmin: (() -> Unit)? = null,
    onLogout: () -> Unit = {},
    isAdmin: Boolean = false
) {
    val context = LocalContext.current
    val preferencesDataStore = remember { PreferencesDataStore(context) }
    val notificationsEnabled by preferencesDataStore.notificationsEnabled().collectAsState(initial = true)
    val darkModeEnabled by preferencesDataStore.darkModeEnabled().collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notificaciones")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesDataStore.setNotificationsEnabled(it) }
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Mode")
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesDataStore.setDarkModeEnabled(it) }
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onHome) { Text("Ir a Home") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onProfile) { Text("Perfil") }
            if (isAdmin && onAdmin != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAdmin) { Text("Admin") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}
