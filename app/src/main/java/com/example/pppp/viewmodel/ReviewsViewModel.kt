package com.example.pppp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.repository.ReviewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class ReviewsViewModel(private val repository: ReviewsRepository) : ViewModel() {
    private val _createResult = MutableStateFlow<Response<Review>?>(null)
    val createResult: StateFlow<Response<Review>?> = _createResult

    fun createReview(request: ReviewRequest, token: String) {
        viewModelScope.launch {
            try {
                val response = repository.createReview(request, token)
                _createResult.value = response
            } catch (_: Exception) {
                _createResult.value = null
            }
        }
    }
}