package com.seers.servicecheck.data

data class LocationData(
    var code: Int,
    var name: String,
    var latitude: Double,
    var longitude: Double,
    var radius: Int = 50,
    var isInside: Boolean = false
)