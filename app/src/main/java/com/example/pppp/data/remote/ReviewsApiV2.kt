package com.example.pppp.data.remote

import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewsApiV2 {
    @POST("/api/reviews")
    suspend fun createReview(
        @Body request: ReviewRequest,
        @Header("Authorization") token: String
    ): Response<Review>

    @GET("/api/reviews/movie/{movieId}")
    suspend fun getReviewsByMovie(
        @Path("movieId") movieId: Long
    ): Response<List<Review>>
}
