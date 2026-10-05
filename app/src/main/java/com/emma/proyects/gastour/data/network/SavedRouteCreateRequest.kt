package com.emma.proyects.gastour.data.network

import com.google.gson.annotations.SerializedName

data class SavedRouteCreateRequest(
    @SerializedName("origin_name") val originName: String,
    @SerializedName("destination_name") val destinationName: String,
    @SerializedName("origin_lat") val originLat: Double,
    @SerializedName("origin_lng") val originLng: Double,
    @SerializedName("dest_lat") val destLat: Double,
    @SerializedName("dest_lng") val destLng: Double,
    @SerializedName("vehicle_model") val vehicleModel: String,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("fuel_liters") val fuelLiters: Double
)