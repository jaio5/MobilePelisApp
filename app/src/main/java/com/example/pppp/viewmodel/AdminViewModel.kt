package com.example.pppp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.uiState.AdminUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState

    // Nuevo método: buscar usuario por email
    fun searchUserByEmail(token: String, email: String) {
        Log.d("ADMIN_USERS", "Buscando usuario por email: $email")
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                if (token.isBlank()) {
                    val errorMsg = "Token vacío. No se puede buscar usuario."
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                    return@launch
                }
                val response = repository.searchUserByEmail(token, email)
                Log.d("ADMIN_USERS", "Petición GET /api/admin/users/search/email?value=$email con token: $token")
                Log.d("ADMIN_USERS", "Respuesta de la API: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.d("ADMIN_USERS", "Usuario encontrado: $user")
                        _uiState.value = AdminUiState.Success(listOf(user))
                    } else {
                        val errorMsg = "No se encontró usuario con ese email."
                        Log.e("ADMIN_USERS", errorMsg)
                        _uiState.value = AdminUiState.Error(errorMsg)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()} ${response.message()}\nError body: $errorBody"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val stackTrace = Log.getStackTraceString(e)
                val errorMsg = "Excepción: ${e.message}\n$stackTrace"
                Log.e("ADMIN_USERS", errorMsg)
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }

    // Nuevo método: buscar usuario por username
    fun searchUserByUsername(token: String, username: String) {
        Log.d("ADMIN_USERS", "Buscando usuario por username: $username")
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                if (token.isBlank()) {
                    val errorMsg = "Token vacío. No se puede buscar usuario."
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                    return@launch
                }
                val response = repository.searchUserByUsername(token, username)
                Log.d("ADMIN_USERS", "Petición GET /api/admin/users/search/username?value=$username con token: $token")
                Log.d("ADMIN_USERS", "Respuesta de la API: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.d("ADMIN_USERS", "Usuario encontrado: $user")
                        _uiState.value = AdminUiState.Success(listOf(user))
                    } else {
                        val errorMsg = "No se encontró usuario con ese username."
                        Log.e("ADMIN_USERS", errorMsg)
                        _uiState.value = AdminUiState.Error(errorMsg)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()} ${response.message()}\nError body: $errorBody"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val stackTrace = Log.getStackTraceString(e)
                val errorMsg = "Excepción: ${e.message}\n$stackTrace"
                Log.e("ADMIN_USERS", errorMsg)
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }

    // Cargar todos los usuarios (admin)
    // Esta función debe ser llamada desde la UI o el repositorio para evitar el warning de función no usada.
    // Si no se va a usar, se puede eliminar o comentar.
    fun loadAllUsers(token: String, roles: List<String>) {
        val tokenPreview = if (token.length > 15) token.take(7) + "..." + token.takeLast(7) else token
        val endpoint = "/api/admin/users"
        Log.d("ADMIN_USERS", "Iniciando carga de usuarios con token: $tokenPreview y roles: $roles")
        _uiState.value = AdminUiState.Loading
        Log.d("ADMIN_USERS", "Token enviado a la API: '$token'")
        if (!token.startsWith("eyJ")) {
            Log.e("ADMIN_USERS", "El token no parece un JWT válido. Token: $token")
        }
        viewModelScope.launch {
            try {
                if (token.isBlank()) {
                    val errorMsg = "Token vacío. No se puede cargar usuarios.\nToken: $tokenPreview"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                    return@launch
                }
                val hasAdminRole = roles.any {
                    val roleNorm = it.trim().uppercase()
                    roleNorm == "ROLE_ADMIN" || roleNorm == "ADMIN"
                }
                Log.d("ADMIN_USERS", "Roles comprobados: $roles | ¿Tiene admin?: $hasAdminRole")
                if (!hasAdminRole) {
                    val errorMsg = "El usuario no tiene permisos de administrador. Roles actuales: $roles"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                    return@launch
                }
                Log.d("ADMIN_USERS", "Petición GET $endpoint con header Authorization: Bearer $tokenPreview")
                val response = repository.getAllUsers(token)
                Log.d("ADMIN_USERS", "Respuesta de la API: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        Log.d("ADMIN_USERS", "Usuarios recibidos: $users")
                        _uiState.value = AdminUiState.Success(users)
                    } else {
                        val errorMsg = "La respuesta de usuarios es nula.\nToken: $tokenPreview\nRoles: $roles"
                        Log.e("ADMIN_USERS", errorMsg)
                        _uiState.value = AdminUiState.Error(errorMsg)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()} ${response.message()}\nEndpoint: $endpoint\nToken: $tokenPreview\nRoles: $roles\nError body: $errorBody"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val stackTrace = Log.getStackTraceString(e)
                val errorMsg = "Excepción: ${e.message}\n$stackTrace\nToken: $tokenPreview\nRoles: $roles"
                Log.e("ADMIN_USERS", errorMsg)
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }

    fun deleteUser(token: String, id: Long, currentUserId: Long) {
        if (id == currentUserId) {
            _uiState.value = AdminUiState.SelfDeleteError
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteUser(token, id)
                Log.d("ADMIN_USERS", "Petición POST /api/admin/users/$id/delete con token: $token")
                Log.d("ADMIN_USERS", "Respuesta de la API: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.UserDeleted(id)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()} ${response.message()}\nError body: $errorBody"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val stackTrace = Log.getStackTraceString(e)
                val errorMsg = "Excepción: ${e.message}\n$stackTrace"
                Log.e("ADMIN_USERS", errorMsg)
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }

    fun banUser(token: String, id: Long) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.banUser(token, id)
                Log.d("ADMIN_USERS", "Petición POST /api/admin/users/$id/ban con token: $token")
                Log.d("ADMIN_USERS", "Respuesta de la API: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _uiState.value = AdminUiState.UserUpdated(user)
                    } else {
                        _uiState.value = AdminUiState.Error("Usuario actualizado pero no recibido en la respuesta.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()} ${response.message()}\nError body: $errorBody"
                    Log.e("ADMIN_USERS", errorMsg)
                    _uiState.value = AdminUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val stackTrace = Log.getStackTraceString(e)
                val errorMsg = "Excepción: ${e.message}\n$stackTrace"
                Log.e("ADMIN_USERS", errorMsg)
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }
}
