package com.emma.proyects.gastour.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gastour_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit {
            putString("jwt_token", token)
        }
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clearToken() {
        prefs.edit {
            remove("jwt_token")
        }
    }
}