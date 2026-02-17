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

    private val _review = MutableStateFlow<Response<Review>?>(null)
    val review: StateFlow<Response<Review>?> = _review

    private val _likeResult = MutableStateFlow<Response<Unit>?>(null)
    val likeResult: StateFlow<Response<Unit>?> = _likeResult

    fun createReview(request: ReviewRequest, token: String) {
        viewModelScope.launch {
            _review.value = repository.createReview(request, token)
        }
    }

    fun likeReview(reviewId: Long, userId: Long, token: String) {
        viewModelScope.launch {
             try {
                val response = repository.likeReview(reviewId, userId, token)
                if (response.isSuccessful) {
                    _likeResult.value = response
                } else {
                    _likeResult.value = null
                }
            } catch (e: Exception) {
                _likeResult.value = null
            }
        }
    }
}