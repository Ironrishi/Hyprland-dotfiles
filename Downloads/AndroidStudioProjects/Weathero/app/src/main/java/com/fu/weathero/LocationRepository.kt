package com.fu.weathero

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class LocationUpdate(val lat: Double, val lon: Double)

object LocationRepository {
    private val _locationFlow = MutableSharedFlow<LocationUpdate>(replay = 1)
    val locationFlow = _locationFlow.asSharedFlow()

    suspend fun emit(lat: Double, lon: Double) {
        _locationFlow.emit(LocationUpdate(lat, lon))
    }
}
