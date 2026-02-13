package com.example.pelisapp.data.remote

import com.example.pelisapp.data.remote.dataclass.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewsApi {
    @POST("/api/reviews")
    suspend fun createReview(
        @Body request: ReviewRequest,
        @Header("Authorization") token: String
    ): Response<Review>

    @POST("/api/reviews/{id}/like")
    suspend fun likeReview(
        @Path("id") reviewId: Long,
        @Query("userId") userId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>
}