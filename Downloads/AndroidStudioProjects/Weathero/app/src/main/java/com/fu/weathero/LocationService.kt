package com.fu.weathero

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationHandlerThread: HandlerThread
    private lateinit var locationLooper: Looper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // FIX: Record when the service started so we can reject any location fix
    // whose timestamp predates service launch. The fused provider sometimes
    // replays a cached fix from a previous session even after flushLocations()
    // completes, because the flush is fire-and-forget and not awaited before
    // the first callback arrives. Comparing elapsedRealtimeNanos ensures we
    // only accept fixes that were produced AFTER this service instance started.
    private val serviceStartElapsedNanos = SystemClock.elapsedRealtimeNanos()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationHandlerThread = HandlerThread("LocationHandlerThread").also { it.start() }
        locationLooper = locationHandlerThread.looper

        createNotification()
        setupLocationCallback()
        flushLastKnownLocation()
        requestLocationUpdates()

        Log.d("LocationService", "Service created, start nanos=$serviceStartElapsedNanos")
    }

    private fun createNotification() {
        val channelId = "location_channel"
        val channel = NotificationChannel(
            channelId, "weather info loading", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        startForeground(
            1, NotificationCompat.Builder(this, channelId)
                .setContentTitle("Weathero running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        )
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.locations.lastOrNull() ?: return

                Log.d(
                    "Location",
                    "lat=${location.latitude} lon=${location.longitude} " +
                    "accuracy=${location.accuracy}m provider=${location.provider} " +
                    "elapsedNanos=${location.elapsedRealtimeNanos}"
                )

                // FIX: Reject any fix whose elapsed-realtime timestamp is older than
                // the moment this service started. This catches stale cached fixes that
                // slip through even after flushLocations() because the flush completes
                // asynchronously — the fused provider can still replay a cached result
                // before the flush takes effect on the first callback.
                if (location.elapsedRealtimeNanos < serviceStartElapsedNanos) {
                    Log.w(
                        "Location",
                        "Ignoring pre-service cached fix " +
                        "(fixNanos=${location.elapsedRealtimeNanos} < startNanos=$serviceStartElapsedNanos)"
                    )
                    return
                }

                // Drop low-accuracy fixes (cell/WiFi guesses)
                if (location.accuracy > 50f) {
                    Log.w("Location", "Ignoring low-accuracy fix (${location.accuracy}m)")
                    return
                }

                fetchWeatherAndStore(location)
            }
        }
    }

    private fun flushLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.flushLocations().addOnCompleteListener {
            Log.d("Location", "Stale location cache flushed")
        }
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1 * 60 * 1000L
        )
            .setMinUpdateIntervalMillis(1 * 60 * 1000L)
            .setMinUpdateDistanceMeters(50f)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setWaitForAccurateLocation(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Location", "Location permission not granted")
            return
        }

        fusedLocationClient.requestLocationUpdates(request, locationCallback, locationLooper)
        Log.d("Location", "Location updates requested")
    }

    private fun fetchWeatherAndStore(location: Location) {
        serviceScope.launch {
            try {
                val weather = fetchWeather(location.latitude, location.longitude)
                val temp = weather.current.temperature_2m

                val deviceId = getDeviceId(applicationContext)
                FirebaseClient.sendData(deviceId, location.latitude, location.longitude, temp)

                WeatherViewModel.pendingLocation.emit(Pair(location.latitude, location.longitude))

            } catch (e: Exception) {
                Log.e("Weather", "Error fetching weather: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationHandlerThread.quitSafely()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
