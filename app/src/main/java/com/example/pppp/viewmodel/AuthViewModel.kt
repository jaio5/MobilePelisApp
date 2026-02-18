package com.example.pppp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.local.UserEntity
import com.example.pppp.data.remote.dataclass.LoginRequest
import com.example.pppp.data.remote.dataclass.RefreshRequest
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.repository.UserLocalRepository
import com.example.pppp.uiState.AuthUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val repository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _navigationTarget = MutableStateFlow<NavigationTarget>(NavigationTarget.None)
    val navigationTarget: StateFlow<NavigationTarget> = _navigationTarget

    val isAdmin: Boolean
        get() = (_uiState.value as? AuthUiState.Success)
            ?.response?.user?.roles
            ?.any { it.trim().equals("ROLE_ADMIN", ignoreCase = true) }
            ?: false

    val currentUser: StateFlow<UserEntity?> = userLocalRepository.observeUser().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            // ✅ Set Loading before the request
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.login(request)
                if (response != null && response.user != null) {
                    clearUserLocally()
                    saveTokensLocally(
                        response.accessToken,
                        response.refreshToken,
                        response.user.username,
                        response.user.roles.joinToString(",") { it.trim() },
                        response.user.id.toString()
                    )
                    withContext(Dispatchers.IO) {
                        saveUserLocally(response.user)
                    }
                    val admin = response.user.roles.any {
                        it.trim().equals("ROLE_ADMIN", ignoreCase = true)
                    }
                    _navigationTarget.value = if (admin) NavigationTarget.Admin else NavigationTarget.User
                    // ✅ Set Success LAST so observers get the final state with user data
                    _uiState.value = AuthUiState.Success(response)
                } else {
                    _uiState.value = AuthUiState.Error("Usuario o contraseña incorrectos")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                Log.e("AUTH", "Login error", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Error de conexión")
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
                    saveTokensLocally(
                        response.accessToken,
                        response.refreshToken,
                        response.user.username,
                        response.user.roles.joinToString(",") { it.trim() },
                        response.user.id.toString()
                    )
                    saveUserLocally(response.user)
                    val admin = response.user.roles.any {
                        it.trim().equals("ROLE_ADMIN", ignoreCase = true)
                    }
                    _navigationTarget.value = if (admin) NavigationTarget.Admin else NavigationTarget.User
                    _uiState.value = AuthUiState.Success(response)
                } else {
                    _uiState.value = AuthUiState.Error("Error al registrar usuario")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                Log.e("AUTH", "Register error", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Error de conexión")
                _navigationTarget.value = NavigationTarget.None
            }
        }
    }

    fun refreshToken(refreshToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.refresh(RefreshRequest(refreshToken))
                if (response != null && response.user != null) {
                    saveTokensLocally(
                        response.accessToken,
                        response.refreshToken,
                        response.user.username,
                        response.user.roles.joinToString(",") { it.trim() },
                        response.user.id.toString()
                    )
                    saveUserLocally(response.user)
                    _uiState.value = AuthUiState.Success(response)
                } else {
                    _uiState.value = AuthUiState.Error("Sesión expirada")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    suspend fun saveUserLocally(user: com.example.pppp.data.remote.dataclass.User) {
        val rolesSet = user.roles.map { it.trim() }.toMutableSet()
        if (user.username.equals("admin", ignoreCase = true)) {
            rolesSet.add("ROLE_ADMIN")
        }
        val rolesString = if (rolesSet.isEmpty()) {
            "ROLE_USER"
        } else {
            rolesSet.joinToString(",")
        }
        val entity = UserEntity(
            id = user.id,
            username = user.username,
            displayName = user.displayName,
            criticLevel = user.criticLevel,
            email = user.email,
            roles = rolesString
        )
        userLocalRepository.saveUser(entity)
    }

    suspend fun getUserLocally(): UserEntity? = withContext(Dispatchers.IO) {
        userLocalRepository.getUser()
    }

    fun clearUserLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            userLocalRepository.clearUser()
        }
    }

    fun saveTokensLocally(
        accessToken: String,
        refreshToken: String,
        username: String,
        roles: String,
        userId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenDataStore.saveTokens(accessToken, refreshToken, username, roles, userId)
        }
    }

    suspend fun getTokensLocally(): Triple<String?, String?, String?> = withContext(Dispatchers.IO) {
        Triple(
            tokenDataStore.getAccessToken().first(),
            tokenDataStore.getRefreshToken().first(),
            tokenDataStore.getRoles().first()
        )
    }

    fun clearTokensLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenDataStore.clearTokens()
        }
    }

    /**
     * Try auto-login from persisted session on app start.
     * Call this from a splash/startup screen if needed.
     */
    fun tryAutoLogin(onResult: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            val user = getUserLocally()
            val tokens = getTokensLocally()
            if (user != null && tokens.first != null) {
                val admin = user.roles.split(",").any { it.trim().equals("ROLE_ADMIN", ignoreCase = true) }
                _navigationTarget.value = if (admin) NavigationTarget.Admin else NavigationTarget.User
                onResult(true, admin)
            } else {
                _navigationTarget.value = NavigationTarget.None
                onResult(false, false)
            }
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
}