package com.connectify.connectifyNow.services

import com.connectify.connectifyNow.models.LocationsResponse
import retrofit.Call
import retrofit.http.Body
import retrofit.http.Headers
import retrofit.http.POST

interface LocationsApiService {
    @Headers(
        "Content-Type: application/json",
        "X-API-KEY: f7f57178224d19338a67a09532470b35ed62dc08"
    )
    @POST("places")
    fun getLocations(@Body body: Map<String, Any>): Call<LocationsResponse>
}