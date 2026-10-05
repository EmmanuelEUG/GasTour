package com.emma.proyects.gastour.data.network

import com.google.gson.annotations.SerializedName

data class UserChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)