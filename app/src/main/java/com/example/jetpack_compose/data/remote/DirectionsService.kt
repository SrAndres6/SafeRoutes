package com.example.jetpack_compose.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("mode") mode: String // "walking" o "bicycling"
    ): Response<DirectionsResponse>
}
