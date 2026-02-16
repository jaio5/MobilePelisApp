package com.example.pppp.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pppp.data.datastore.PreferencesDataStore
import com.example.pppp.viewmodel.UserViewModel
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.data.remote.RetrofitClient
import com.example.pppp.data.datastore.TokenDataStore
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onHome: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(RetrofitClient.instance.create(com.example.pppp.data.remote.UserApi::class.java)) }
    val userViewModel: UserViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
    })
    val preferencesDataStore = remember { PreferencesDataStore(context) }
    val userState by userViewModel.user.collectAsState()
    val notificationsEnabled by preferencesDataStore.notificationsEnabled().collectAsState(initial = true)
    val darkModeEnabled by preferencesDataStore.darkModeEnabled().collectAsState(initial = false)
    val tokenDataStore = remember { TokenDataStore(context) }
    val accessToken by tokenDataStore.getAccessToken().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    LaunchedEffect(accessToken) {
        accessToken?.let { userViewModel.getMe(it) }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Perfil de usuario", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (userState?.isSuccessful == true) {
                val user = userState?.body()
                Text("Usuario: ${user?.username ?: "-"}")
                Text("Email: ${user?.email ?: "-"}")
                user?.displayName?.let { Text("Nombre: $it") }
                user?.criticLevel?.let { Text("Nivel de crítico: $it") }
                user?.roles?.let { Text("Roles: ${it.joinToString(", ")}") }
                Spacer(modifier = Modifier.height(8.dp))
                accessToken?.let { Text("Token: $it", style = MaterialTheme.typography.bodySmall) }
                if (user?.roles?.contains("ROLE_ADMIN") == true) {
                    Button(onClick = onAdmin) { Text("Panel de Admin") }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text("Cargando datos de usuario...")
            }
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
            Button(onClick = onSettings) { Text("Ajustes") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onHome) { Text("Ir a Home") }
            Button(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}