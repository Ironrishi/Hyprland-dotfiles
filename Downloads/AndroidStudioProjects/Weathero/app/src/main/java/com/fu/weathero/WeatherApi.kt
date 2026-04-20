package com.fu.weathero

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ─────────────────────────────────────────────
// Response models
// ─────────────────────────────────────────────

data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val weather_code: Int
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: DailyWeather
)

data class CurrentAQI(
    val us_aqi: Int
)

data class AQIResponse(
    val current: CurrentAQI
)

// ─────────────────────────────────────────────
// API interfaces
//
// FIX: Retrofit does NOT support Kotlin default parameter values on @Query.
// If a default is declared in the interface, Retrofit ignores it and the
// parameter is either omitted or sent as an empty string — causing the API
// to return a shape that doesn't match our data classes, producing wrong
// or zero values. All query params are now hardcoded in a wrapper function
// below so they are always explicitly passed.
// ─────────────────────────────────────────────

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int
    ): WeatherResponse
}

interface AirQualityApi {

    @GET("v1/air-quality")
    suspend fun getAQI(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String
    ): AQIResponse
}

// ─────────────────────────────────────────────
// Retrofit singletons
// ─────────────────────────────────────────────

object RetrofitInstance {

    private val weatherRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val airRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApi: WeatherApi by lazy {
        weatherRetrofit.create(WeatherApi::class.java)
    }

    val airApi: AirQualityApi by lazy {
        airRetrofit.create(AirQualityApi::class.java)
    }
}

// ─────────────────────────────────────────────
// Wrapper functions — always pass all required
// query params explicitly so nothing is omitted
// ─────────────────────────────────────────────

suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse {
    return RetrofitInstance.weatherApi.getWeather(
        lat = lat,
        lon = lon,
        current = "temperature_2m,weather_code",
        daily = "temperature_2m_max,temperature_2m_min",
        timezone = "auto",
        forecastDays = 1
    )
}

suspend fun fetchAQI(lat: Double, lon: Double): AQIResponse {
    return RetrofitInstance.airApi.getAQI(
        lat = lat,
        lon = lon,
        current = "us_aqi"
    )
}
