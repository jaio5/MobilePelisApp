package com.example.pppp.data.remote

import androidx.compose.foundation.content.MediaType
import retrofit2.Retrofit
import kotlin.jvm.java

object Retrofit {
    const val BASE_URL = "http://10.0.2.2:8080/"

    const val CONTENT_TYPE = "application/json"


    private val json = Json{
        ignoreUknownKeys = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(contentType = MediaType.get(CONTENT_TYPE)))
        .build()

    val apiServiceApi: ServiceApi = retrofit.create(ServiceApi::class.java)
}
