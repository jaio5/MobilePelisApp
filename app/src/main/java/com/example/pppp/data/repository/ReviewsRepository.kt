package com.example.pppp.data.repository

import com.example.pppp.data.remote.ReviewsApi
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import retrofit2.Response

class ReviewsRepository(private val api: ReviewsApi = Retrofit.Reviews) {

    suspend fun createReview(request: ReviewRequest, token: String): Response<Review> {
        return api.createReview(request, token)
    }

    suspend fun likeReview(reviewId: Long, userId:Long,token: String): Response<Unit> {
        return api.likeReview(reviewId, userId, token)
    }
}