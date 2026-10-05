package com.emma.proyects.gastour.data.models

import com.google.gson.annotations.SerializedName

data class Vehicle(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("model") val model: String,
    @SerializedName("efficiency_km_l") val efficiencyKmL: Double,
    @SerializedName("weight_kg") val weightKg: Double
)