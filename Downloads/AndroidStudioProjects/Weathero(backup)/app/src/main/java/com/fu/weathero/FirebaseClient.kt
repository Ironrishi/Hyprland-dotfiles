package com.fu.weathero

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseClient {

    private val db = FirebaseFirestore.getInstance()

    fun sendData(lat: Double, lon: Double, temp: Double) {

        val data = hashMapOf(
            "user_id" to "device_1",
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