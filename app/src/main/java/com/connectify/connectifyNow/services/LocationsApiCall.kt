package com.connectify.connectifyNow.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.connectify.connectifyNow.models.Location
import com.connectify.connectifyNow.models.LocationsResponse
import retrofit.Call
import retrofit.Callback
import retrofit.GsonConverterFactory
import retrofit.Response
import retrofit.Retrofit

private const val BASE_URL =
    "https://google.serper.dev/"

class LocationsApiCall {
    fun getLocationsByQuery(context: Context, query: String, callback: (Array<Location>) -> Unit) {
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()

        val apiService: LocationsApiService = retrofit.create(LocationsApiService::class.java)

        val locationsRequestBody = mapOf(
            "q" to query,
            "gl" to "il"
        )

        val call: Call<LocationsResponse> = apiService.getLocations(locationsRequestBody)
        call.enqueue(object: Callback<LocationsResponse> {
            override fun onResponse(response: Response<LocationsResponse>, retrofit: Retrofit?) {
                val res: LocationsResponse = response.body()
                Log.d("Success", "locations:" + res.places.size.toString() + res.toString())
                callback(res.places)
            }

            override fun onFailure(t: Throwable?) {
                Log.d("Error", "locations: "+ t?.message)
                Toast.makeText(context, "Request Fail", Toast.LENGTH_SHORT).show()
            }
        })
    }
}