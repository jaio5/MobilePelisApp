package com.example.pppp.data.repository

import android.util.Log
import com.example.pppp.data.remote.AuthApi
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.AuthResponse
import com.example.pppp.data.remote.dataclass.LoginRequest
import com.example.pppp.data.remote.dataclass.RefreshRequest
import com.example.pppp.data.remote.dataclass.RegisterRequest

class AuthRepository(private val api: AuthApi = Retrofit.apiAuth) {

    /**
     * Returns AuthResponse on success, throws Exception on HTTP error so the
     * ViewModel can surface a meaningful message to the user.
     */
    suspend fun login(request: LoginRequest): AuthResponse? {
        return try {
            val response = api.login(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("AUTH_REPO", "Login HTTP ${response.code()}: $errorBody")
                throw Exception(parseErrorMessage(errorBody, response.code()))
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "Login exception", e)
            throw e
        }
    }

    suspend fun register(request: RegisterRequest): AuthResponse? {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("AUTH_REPO", "Register HTTP ${response.code()}: $errorBody")
                throw Exception(parseErrorMessage(errorBody, response.code()))
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "Register exception", e)
            throw e
        }
    }

    suspend fun refresh(request: RefreshRequest): AuthResponse? {
        return try {
            val response = api.refresh(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("AUTH_REPO", "Refresh HTTP ${response.code()}: $errorBody")
                throw Exception(parseErrorMessage(errorBody, response.code()))
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "Refresh exception", e)
            throw e
        }
    }


    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val json = org.json.JSONObject(body)
            json.optString("message", null)
                ?: json.optString("error", null)
                ?: httpErrorMessage(code)
        } catch (e: Exception) {
            httpErrorMessage(code)
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "Datos de inicio de sesión inválidos"
        401 -> "Usuario o contraseña incorrectos"
        403 -> "Acceso denegado"
        404 -> "Servicio no encontrado"
        409 -> "El usuario ya existe"
        500 -> "Error interno del servidor"
        else -> "Error HTTP $code"
    }
}