package com.connectify.connectifyNow.services

import com.connectify.connectifyNow.models.LocationsResponse
import retrofit.Call
import retrofit.http.Body
import retrofit.http.Headers
import retrofit.http.POST

interface LocationsApiService {
    @Headers(
        "Content-Type: application/json",
        "X-API-KEY: 6ad2bb4f22fca025e6fad9dae09f951eb1c6ab71"
    )
    @POST("locations")
    fun getLocations(@Body body: Map<String, Any>): Call<LocationsResponse>
}