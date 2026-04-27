package com.example.smartvendor.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}

data class WeatherResponse(
    val main: Main,
    val name: String
)

data class Main(
    val temp: Double
)
