package com.emma.proyects.gastour.data.network

import com.google.gson.annotations.SerializedName

data class UserUpdateUsernameRequest(
    @SerializedName("username") val username: String
)