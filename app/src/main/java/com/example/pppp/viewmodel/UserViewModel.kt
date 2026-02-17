package com.example.pppp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.remote.dataclass.PaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.User
import com.example.pppp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(private val repository: UserRepository): ViewModel() {

    private val _user = MutableStateFlow<Response<User>?>(null)
    val user: StateFlow<Response<User>?> = _user

    private val _myReviews = MutableStateFlow<Response<PaginatedResponse<Review>>?>(null)
    val myReviews: StateFlow<Response<PaginatedResponse<Review>>?> = _myReviews

    fun getMe(token: String){
        viewModelScope.launch{
            try {
                val response = repository.getMe(token)
                if (response.isSuccessful) {
                    _user.value = response
                } else {
                    _user.value = null
                }
            } catch (e: Exception) {
                _user.value = null
            }
        }
    }

    fun getMyReviews(token: String, page: Int, size: Int){
        viewModelScope.launch{
            try {
                val response = repository.getMyReviews(token,page, size)
                if (response.isSuccessful) {
                    _myReviews.value = response
                } else {
                    _myReviews.value = null
                }
            } catch (e: Exception) {
                _myReviews.value = null
            }
        }
    }
}