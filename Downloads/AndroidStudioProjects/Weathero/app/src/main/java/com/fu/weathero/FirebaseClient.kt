package com.fu.weathero

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseClient {

    private val db = FirebaseFirestore.getInstance()

    // FIX: deviceId is now passed as a parameter instead of referencing an undefined variable
    fun sendData(deviceId: String, lat: Double, lon: Double, temp: Double) {
        val data = hashMapOf(
            "user_id" to deviceId,
            "latitude" to lat,
            "longitude" to lon,
            "temperature" to temp,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("weather_logs")
            .add(data)
            .addOnSuccessListener {
                Log.d("Firebase", "Data stored successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error storing data", e)
            }
    }
}
