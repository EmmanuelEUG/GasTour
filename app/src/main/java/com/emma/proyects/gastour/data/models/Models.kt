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

data class SavedRoute(
    val id: Int,
    val originName: String,
    val destinationName: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val vehicleModel: String,
    val distanceKm: Double,
    val fuelLiters: Double,
    val date: String
)