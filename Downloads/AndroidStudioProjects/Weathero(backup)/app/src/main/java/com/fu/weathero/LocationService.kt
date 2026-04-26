package com.fu.weathero

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = OkHttpClient()

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotification()
        setupLocationCallback()
        requestLocationUpdates()
        Log.d("LocationService", "Service created")
    }

    private fun createNotification() {
        val channelId = "location_channel"

        val channel = NotificationChannel(
            channelId,
            "weather info loading",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Weathero running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val lat = location.latitude
                val lon = location.longitude

                Log.d("Location", "Got location: $lat, $lon")

                // TEMP test value for temperature
                fetchWeatherAndStore(location)
            }

        }
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            500L
        )
            .setMinUpdateIntervalMillis(200L)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Location", "Location permission not granted, cannot start updates")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        Log.d("Location", "Location updates requested")
    }

    private fun fetchWeatherAndStore(location: Location) {
        serviceScope.launch {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=${location.latitude}" +
                        "&longitude=${location.longitude}" +
                        "&current_weather=true"

                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string() ?: run {
                    Log.e("Weather", "Empty response body")
                    return@launch
                }

                Log.d("Weather", "Weather response code: ${response.code}")
                Log.d("Weather", "Weather body: $body")

                val json = JSONObject(body)
                val weather = json.getJSONObject("current_weather")
                val temp = weather.getDouble("temperature")

                FirebaseClient.sendData(location.latitude, location.longitude, temp)

            } catch (e: Exception) {
                Log.e("Weather", "Error fetching weather: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}