package com.example.pppp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.remote.dataclass.LoginRequest
import com.example.pppp.data.remote.dataclass.RegisterRequest
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
    private val tokenDataStore: TokenDataStore,
    private val appContext: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _navigationTarget = MutableStateFlow<NavigationTarget>(NavigationTarget.None)
    val navigationTarget: StateFlow<NavigationTarget> = _navigationTarget

    private var _isAdmin: Boolean = false
    val isAdmin: Boolean
        get() = _isAdmin

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

    suspend fun getUserLocally(): com.example.pppp.data.local.UserEntity? {
        return withContext(Dispatchers.IO) {
            userLocalRepository.getUser()
        }
    }

    fun clearUserLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            userLocalRepository.clearUser()
        }
    }

    fun saveTokensLocally(accessToken: String, refreshToken: String, username: String, roles: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenDataStore.saveTokens(accessToken, refreshToken, username, roles)
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

    fun login(username: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                val response = repository.login(request)
                Log.d("API_LOGIN", "AuthResponse: $response")
                if (response != null && response.user != null) {
                    Log.d("API_LOGIN", "Roles: ${response.user.roles}")
                    _isAdmin = response.user.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
                    _uiState.value = AuthUiState.Success(response)
                    saveUserLocally(response.user)
                    saveTokensLocally(response.accessToken, response.refreshToken, response.user.username, response.user.roles.joinToString(","))
                    val roles = response.user.roles
                    if (roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }) {
                        _navigationTarget.value = NavigationTarget.Admin
                    } else {
                        _navigationTarget.value = NavigationTarget.User
                    }
                } else {
                    _isAdmin = false
                    _uiState.value = AuthUiState.Error("Respuesta inválida o usuario no recibido")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                _isAdmin = false
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Error desconocido")
                _navigationTarget.value = NavigationTarget.None
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
                    Log.d("API_REFRESH", "Roles: ${response.user.roles}")
                    _uiState.value = AuthUiState.Success(response)
                    val roles = response.user.roles
                    if (roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }) {
                        _navigationTarget.value = NavigationTarget.Admin
                    } else {
                        _navigationTarget.value = NavigationTarget.User
                    }
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

    fun register(username: String, email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, email, password)
                val response = repository.register(request)
                Log.d("API_REGISTER", "AuthResponse: $response")
                if (response != null && response.user != null) {
                    Log.d("API_REGISTER", "Roles: ${response.user.roles}")
                    _isAdmin = response.user.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
                    _uiState.value = AuthUiState.Success(response)
                    val roles = response.user.roles
                    if (roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }) {
                        _navigationTarget.value = NavigationTarget.Admin
                    } else {
                        _navigationTarget.value = NavigationTarget.User
                    }
                } else {
                    _isAdmin = false
                    _uiState.value = AuthUiState.Error("Respuesta inválida o usuario no recibido")
                    _navigationTarget.value = NavigationTarget.None
                }
            } catch (e: Exception) {
                _isAdmin = false
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Error desconocido")
                _navigationTarget.value = NavigationTarget.None
            }
        }
    }

    fun logout() {
        clearUserLocally()
        clearTokensLocally()
        _navigationTarget.value = NavigationTarget.None
        _uiState.value = AuthUiState.Idle
    }
}