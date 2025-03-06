package com.connectify.connectifyNow.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.connectify.connectifyNow.models.NominatimResponse
import retrofit.Call
import retrofit.Callback
import retrofit.GsonConverterFactory
import retrofit.Response
import retrofit.Retrofit

private const val BASE_URL = "https://nominatim.openstreetmap.org/"

class AddressApiCall {
    fun getAddressByLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        callback: (String?) -> Unit
    ) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: AddressApiService = retrofit.create(AddressApiService::class.java)

        val call: Call<NominatimResponse> = apiService.getAddress(latitude, longitude)
        call.enqueue(object : Callback<NominatimResponse> {
            override fun onResponse(response: Response<NominatimResponse>, retrofit: Retrofit?) {
                val res: NominatimResponse? = response.body()
                if (res != null) {
                    Log.d("Success", "Address: ${res.display_name}")
                    callback(res.display_name)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(t: Throwable?) {
                Log.d("Error", "Address fetch failed: ${t?.message}")
                Toast.makeText(context, "Request Failed", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }
}
