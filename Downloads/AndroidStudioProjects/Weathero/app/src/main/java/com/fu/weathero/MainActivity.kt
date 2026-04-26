package com.fu.weathero

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    private val requestBackgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startLocationService()
        }

    private val requestFineLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestBackgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    startLocationService()
                }
            }
        }

    private val requestNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkLocationAndStart()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: setContent is called once and never re-invoked.
        // WeatherScreen observes viewModel.state internally — no activity-level
        // mutableStateOf vars means no spurious recompositions / flickering.
        setContent {
            WeatherScreen(viewModel = viewModel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        checkLocationAndStart()
    }

    private fun checkLocationAndStart() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationService()
        } else {
            requestFineLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startLocationService() {
        if (!isServiceRunning()) {
            val intent = Intent(this, LocationService::class.java)
            startForegroundService(intent)
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == LocationService::class.java.name }
    }
}

fun getDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var id = prefs.getString("device_id", null)
    if (id == null) {
        id = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", id).apply()
    }
    return id
}
