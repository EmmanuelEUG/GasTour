package com.emma.proyects.gastour.data.models

import org.osmdroid.util.GeoPoint

data class User(
    val id: Int,
    val username: String
)

data class RouteOption(
    val nameResId: Int,
    val distanceKm: Double,
    val durationMin: Double,
    val elevationGainMeters: Double,
    val fuelLiters: Double,
    val pathPoints: List<GeoPoint>
)