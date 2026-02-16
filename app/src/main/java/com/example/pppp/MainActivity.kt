package com.example.pppp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.pppp.navigation.NavGraph
import com.example.pppp.data.local.UserDao
import com.example.pppp.data.datastore.PreferencesDataStore
import com.example.pppp.util.NetworkMonitor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.pppp.ui.theme.PelisAppTheme
import androidx.compose.material3.Snackbar
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val preferencesDataStore = remember { PreferencesDataStore(context) }
            val darkModeEnabled by preferencesDataStore.darkModeEnabled().collectAsState(initial = false)
            val networkMonitor = remember { NetworkMonitor(context) }
            val isOnline by networkMonitor.isOnline.collectAsState()
            PelisAppTheme(darkTheme = darkModeEnabled) {
                val navController = rememberNavController()
                val userDao: UserDao = PelisApp.database.userDao()
                if (!isOnline) {
                    // Banner o aviso visual de offline
                    Snackbar(
                        modifier = Modifier.padding(8.dp),
                        action = { },
                        content = { Text("Sin conexión a Internet") }
                    )
                }
                NavGraph(navController = navController, userDao = userDao)
            }
        }
    }
}