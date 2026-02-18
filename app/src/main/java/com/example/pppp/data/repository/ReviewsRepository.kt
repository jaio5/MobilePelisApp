package com.example.pppp.data.repository

import com.example.pppp.data.remote.ReviewsApiV2
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import retrofit2.Response

class ReviewsRepository(
    private val api: ReviewsApiV2 = Retrofit.ReviewsV2
) {
    suspend fun createReview(request: ReviewRequest, token: String): Response<Review> {
        return api.createReview(request, token)
    }

    suspend fun getReviewsByMovie(movieId: Long): Response<List<Review>> {
        return api.getReviewsByMovie(movieId)
    }
}