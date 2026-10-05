package com.emma.proyects.gastour.data.network

import com.google.gson.annotations.SerializedName

data class VehicleCreateRequest(
    @SerializedName("model") val model: String,
    @SerializedName("efficiency_km_l") val efficiencyKmL: Double,
    @SerializedName("weight_kg") val weightKg: Double
)