package com.emma.proyects.gastour.ui.viewmodel
import com.emma.proyects.gastour.data.models.RouteOption
import com.emma.proyects.gastour.data.models.SavedRoute
import com.emma.proyects.gastour.data.models.User
import com.emma.proyects.gastour.data.models.Vehicle

data class AppUiState(
    val currentUser: User? = null,
    val userVehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destLat: Double? = null,
    val destLng: Double? = null,
    val calculatedRoutes: List<RouteOption> = emptyList(),
    val selectedRoute: RouteOption? = null,
    val isLoading: Boolean = false,
    val messageResId: Int? = null,
    val rawErrorMessage: String? = null,
    val savedRoutes: List<SavedRoute> = emptyList()
)