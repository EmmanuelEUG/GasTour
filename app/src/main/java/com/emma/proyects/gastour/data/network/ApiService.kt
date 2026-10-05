package com.emma.proyects.gastour.data.network

import com.emma.proyects.gastour.data.models.SavedRoute
import com.emma.proyects.gastour.data.models.User
import com.emma.proyects.gastour.data.models.Vehicle
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserCreateRequest(
    val username: String,
    val password: String
)

data class VehicleCatalogDto(
    @SerializedName("id") val id: Int,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("year") val year: Int,
    @SerializedName("city_consumption") val cityConsumption: Double,
    @SerializedName("highway_consumption") val highwayConsumption: Double,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("fuel_type") val fuelType: String
)



interface ApiService {
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: UserCreateRequest): Response<TokenResponse>

    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<User>

    @PUT("api/users/me/username")
    suspend fun updateUsername(@Body request: UserUpdateUsernameRequest): Response<User>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body request: UserChangePasswordRequest): Response<Map<String, String>>

    @GET("api/vehicles/catalog")
    suspend fun getVehicleCatalog(): Response<List<VehicleCatalogDto>>

    @GET("api/vehicles")
    suspend fun getVehicles(): Response<List<Vehicle>>

    @POST("api/vehicles")
    suspend fun addVehicle(@Body request: VehicleCreateRequest): Response<Vehicle>

    @PUT("api/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") id: Int, @Body request: VehicleCreateRequest): Response<Vehicle>

    @DELETE("api/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Int): Response<Map<String, String>>

    @GET("api/routes")
    suspend fun getSavedRoutes(): Response<List<SavedRoute>>

    @POST("api/routes")
    suspend fun saveRoute(@Body request: SavedRouteCreateRequest): Response<SavedRoute>
}