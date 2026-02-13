package com.example.pelisapp.data.repository

import com.example.pelisapp.data.remote.ReviewsApi
import com.example.pelisapp.data.remote.dataclass.Review
import com.example.pelisapp.data.remote.dataclass.ReviewRequest
import retrofit2.Response

class ReviewsRepository(private val api: ReviewsApi) {

    suspend fun createReview(request: ReviewRequest, token: String): Response<Review> {
        return api.createReview(request, token)
    }

    suspend fun likeReview(reviewId: Long, userId:Long,token: String): Response<Unit> {
        return api.likeReview(reviewId, userId, token)
    }
}