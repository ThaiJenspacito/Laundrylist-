package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val daily: DailyForecast
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getDailyForecast(
        @Query("latitude") lat: Double = 13.75, // Bangkok
        @Query("longitude") lon: Double = 100.51,
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "Asia/Bangkok",
        @Query("forecast_days") forecastDays: Int = 4 // Today + 3 days
    ): WeatherResponse
    
    companion object {
        fun create(): WeatherApi {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WeatherApi::class.java)
        }
    }
}