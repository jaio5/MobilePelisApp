package com.example.pelisapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pelisapp.data.remote.dataclass.AuthResponse
import com.example.pelisapp.data.remote.dataclass.LoginRequest
import com.example.pelisapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authResponse = MutableStateFlow<Response<AuthResponse>?>(null)
    val authResponse: StateFlow<Response<AuthResponse>?> = _authResponse

    fun login(username: String, password: String){
        viewModelScope.launch{
            val response = repository.login(LoginRequest(username,password))
            _authResponse.value = response
        }
    }
}