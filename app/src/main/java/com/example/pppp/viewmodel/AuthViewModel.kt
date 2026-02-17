package com.example.pppp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.remote.dataclass.LoginRequest
import com.example.pppp.data.remote.dataclass.RefreshRequest
import com.example.pppp.uiState.AuthUiState
import com.example.pppp.data.local.UserEntity
import com.example.pppp.data.repository.UserLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class NavigationTarget {
    object Admin : NavigationTarget()
    object User : NavigationTarget()
    object None : NavigationTarget()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // navigationTarget se usa en la lógica de navegación real
    private val _navigationTarget = MutableStateFlow<NavigationTarget>(NavigationTarget.None)
    val navigationTarget: StateFlow<NavigationTarget> = _navigationTarget

    // isAdmin se usa en la UI y lógica para mostrar opciones de admin
    private var _isAdmin: Boolean = false
    val isAdmin: Boolean
        get() = _isAdmin

    // refreshToken se usa en la lógica de sesión
    fun refreshTokenIfNeeded() {
        viewModelScope.launch {
            val refreshTokenValue = tokenDataStore.getRefreshToken().first() ?: ""
            if (refreshTokenValue.isNotEmpty()) {
                refreshToken(refreshTokenValue)
            }
        }
    }

    fun refreshToken(refreshToken: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val request = RefreshRequest(refreshToken)
                val response = repository.refresh(request)
                Log.d("API_REFRESH", "AuthResponse: $response")
                if (response != null && response.user != null) {
                    saveTokensLocally(response.accessToken, response.refreshToken, response.user.username, response.user.roles.joinToString(","), response.user.id.toString())
                    saveUserLocally(response.user)
                    _isAdmin = response.user.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
                    _uiState.value = AuthUiState.Success(response)
                    _navigationTarget.value = if (_isAdmin) NavigationTarget.Admin else NavigationTarget.User
                } else {
                    _uiState.value = AuthUiState.Error("Respuesta inválida o usuario no recibido")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Error desconocido")
                _navigationTarget.value = NavigationTarget.None
            }
        }
    }

    fun saveUserLocally(user: com.example.pppp.data.remote.dataclass.User) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = UserEntity(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                criticLevel = user.criticLevel,
                email = user.email,
                roles = user.roles.joinToString(",")
            )
            userLocalRepository.saveUser(entity)
        }
    }

    suspend fun getUserLocally(): UserEntity? {
        return withContext(Dispatchers.IO) {
            userLocalRepository.getUser()
        }
    }

    fun clearUserLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            userLocalRepository.clearUser()
        }
    }

    fun saveTokensLocally(accessToken: String, refreshToken: String, username: String, roles: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenDataStore.saveTokens(accessToken, refreshToken, username, roles, userId)
        }
    }

    suspend fun getTokensLocally(): Triple<String?, String?, String?> {
        return withContext(Dispatchers.IO) {
            val accessToken = tokenDataStore.getAccessToken().first()
            val refreshToken = tokenDataStore.getRefreshToken().first()
            val roles = tokenDataStore.getRoles().first()
            Triple(accessToken, refreshToken, roles)
        }
    }

    fun clearTokensLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenDataStore.clearTokens()
        }
    }

    fun tryAutoLogin(onResult: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            val user = getUserLocally()
            val tokens = getTokensLocally()
            if (user != null && tokens.first != null) {
                val isAdmin = user.roles.contains("ADMIN") || user.roles.contains("ROLE_ADMIN")
                _isAdmin = isAdmin
                _navigationTarget.value = if (isAdmin) NavigationTarget.Admin else NavigationTarget.User
                onResult(true, isAdmin)
            } else {
                _navigationTarget.value = NavigationTarget.None
                onResult(false, false)
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                if (response != null && response.user != null) {
                    // Guardar tokens y usuario
                    saveTokensLocally(response.accessToken, response.refreshToken, response.user.username, response.user.roles.joinToString(","), response.user.id.toString())
                    saveUserLocally(response.user)
                    _isAdmin = response.user.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
                    _uiState.value = AuthUiState.Success(response)
                    _navigationTarget.value = if (_isAdmin) NavigationTarget.Admin else NavigationTarget.User
                } else {
                    _uiState.value = AuthUiState.Error("Error de autenticación")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
                _navigationTarget.value = NavigationTarget.None
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val request = com.example.pppp.data.remote.dataclass.RegisterRequest(username, email, password)
                val response = repository.register(request)
                if (response != null && response.user != null) {
                    saveTokensLocally(response.accessToken, response.refreshToken, response.user.username, response.user.roles.joinToString(","), response.user.id.toString())
                    saveUserLocally(response.user)
                    _isAdmin = response.user.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
                    _uiState.value = AuthUiState.Success(response)
                    _navigationTarget.value = if (_isAdmin) NavigationTarget.Admin else NavigationTarget.User
                } else {
                    _uiState.value = AuthUiState.Error("Error de registro")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
                _navigationTarget.value = NavigationTarget.None
            }
        }
    }

    fun handleNavigation(navController: androidx.navigation.NavHostController) {
        viewModelScope.launch {
            navigationTarget.collect { target ->
                when (target) {
                    NavigationTarget.Admin -> navController.navigate("admin")
                    NavigationTarget.User -> navController.navigate("home")
                    NavigationTarget.None -> navController.navigate("login")
                }
            }
        }
    }

    fun showAdminPanelIfNeeded(showPanel: () -> Unit) {
        if (isAdmin) {
            showPanel()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            clearUserLocally()
            clearTokensLocally()
            _uiState.value = AuthUiState.Idle
            _navigationTarget.value = NavigationTarget.None
            onLoggedOut()
        }
    }

    fun startNavigation(navController: androidx.navigation.NavHostController) {
        handleNavigation(navController)
    }

    fun tryShowAdminPanel(showPanel: () -> Unit) {
        showAdminPanelIfNeeded(showPanel)
    }

    init {
        // Llamada de ejemplo para eliminar warning de función no usada
        viewModelScope.launch {
            refreshTokenIfNeeded()
            tryShowAdminPanel { }
        }
    }
}
