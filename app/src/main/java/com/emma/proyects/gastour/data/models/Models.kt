package com.emma.proyects.gastour.data.models

import org.osmdroid.util.GeoPoint

data class User(
    val id: Int,
    val username: String
)

data class Vehicle(
    val id: Int,
    val model: String,
    val efficiencyKmL: Double,
    val weightKg: Double
)

data class RouteOption(
    val nameResId: Int, // String Resource ID for localization
    val distanceKm: Double,
    val durationMin: Double,
    val elevationGainMeters: Double,
    val fuelLiters: Double,
    val pathPoints: List<GeoPoint>
)