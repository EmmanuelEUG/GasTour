package com.emma.proyects.gastour.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.RouteOption
import com.emma.proyects.gastour.data.models.SavedRoute
import com.emma.proyects.gastour.data.models.Vehicle
import com.emma.proyects.gastour.data.network.ApiClient
import com.emma.proyects.gastour.data.network.SavedRouteCreateRequest
import com.emma.proyects.gastour.data.network.TokenManager
import com.emma.proyects.gastour.data.network.UserChangePasswordRequest
import com.emma.proyects.gastour.data.network.UserCreateRequest
import com.emma.proyects.gastour.data.network.UserUpdateUsernameRequest
import com.emma.proyects.gastour.data.network.VehicleCatalogDto
import com.emma.proyects.gastour.data.network.VehicleCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.round

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getApiService(application)
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        if (tokenManager.getToken() != null) {
            loadUserData()
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userRes = apiService.getCurrentUser()
                val vehiclesRes = apiService.getVehicles()
                val routesRes = apiService.getSavedRoutes()

                if (userRes.isSuccessful) {
                    val user = userRes.body()
                    val vehicles = vehiclesRes.body() ?: emptyList()
                    val routes = routesRes.body() ?: emptyList()

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            userVehicles = vehicles,
                            selectedVehicle = vehicles.firstOrNull(),
                            savedRoutes = routes,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, rawErrorMessage = e.message) }
            }
        }
    }

    fun loadVehicleCatalog(onSuccess: (List<VehicleCatalogDto>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = apiService.getVehicleCatalog()
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(rawErrorMessage = e.message) }
            }
        }
    }

    fun checkVehiclesAndNavigate(onNavigateToAddVehicle: () -> Unit, onNavigateToMap: () -> Unit) {
        viewModelScope.launch {
            try {
                val vehiclesRes = apiService.getVehicles()
                if (vehiclesRes.isSuccessful) {
                    val vehicles = vehiclesRes.body() ?: emptyList()
                    if (vehicles.isEmpty()) {
                        onNavigateToAddVehicle()
                    } else {
                        onNavigateToMap()
                    }
                } else {
                    onNavigateToAddVehicle()
                }
            } catch (_: Exception) {
                onNavigateToAddVehicle()
            }
        }
    }

    fun login(user: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, messageResId = null) }
            try {
                val response = apiService.login(user, pass)
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.saveToken(response.body()!!.accessToken)
                    loadUserData()
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, messageResId = R.string.auth_error_fields) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, rawErrorMessage = e.message) }
            }
        }
    }

    fun register(user: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, messageResId = null) }
            try {
                val response = apiService.register(UserCreateRequest(user, pass))
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.saveToken(response.body()!!.accessToken)
                    loadUserData()
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, messageResId = R.string.auth_error_all_fields) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, rawErrorMessage = e.message) }
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        tokenManager.clearToken()
        _uiState.update { AppUiState() }
        onLogoutSuccess()
    }

    fun updateUsername(newUsername: String, onSessionExpired: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.updateUsername(UserUpdateUsernameRequest(newUsername))
                if (response.isSuccessful) {
                    tokenManager.clearToken()
                    _uiState.update { AppUiState() }
                    onSessionExpired()
                }
            } catch (_: Exception) {}
        }
    }

    fun changePassword(currentPass: String, newPass: String, onSessionExpired: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.changePassword(UserChangePasswordRequest(currentPass, newPass))
                if (response.isSuccessful) {
                    tokenManager.clearToken()
                    _uiState.update { AppUiState() }
                    onSessionExpired()
                }
            } catch (_: Exception) {}
        }
    }

    fun addVehicle(model: String, efficiency: Double, weight: Double) {
        viewModelScope.launch {
            try {
                val response = apiService.addVehicle(VehicleCreateRequest(model, efficiency, weight))
                if (response.isSuccessful && response.body() != null) {
                    val newVehicle = response.body()!!
                    _uiState.update { current ->
                        val updatedVehicles = current.userVehicles + newVehicle
                        current.copy(
                            userVehicles = updatedVehicles,
                            selectedVehicle = newVehicle
                        )
                    }
                    loadUserData()
                }
            } catch (_: Exception) {}
        }
    }

    fun updateVehicle(id: Int, model: String, efficiency: Double, weight: Double) {
        viewModelScope.launch {
            try {
                val response = apiService.updateVehicle(id, VehicleCreateRequest(model, efficiency, weight))
                if (response.isSuccessful) {
                    loadUserData()
                }
            } catch (_: Exception) {}
        }
    }

    fun deleteVehicle(id: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteVehicle(id)
                if (response.isSuccessful) {
                    loadUserData()
                }
            } catch (_: Exception) {}
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
    }

    fun setOrigin(lat: Double, lng: Double) {
        _uiState.update { it.copy(originLat = lat, originLng = lng, calculatedRoutes = emptyList()) }
    }

    fun setDestination(lat: Double, lng: Double) {
        _uiState.update { it.copy(destLat = lat, destLng = lng, calculatedRoutes = emptyList()) }
    }

    fun clearPoints() {
        _uiState.update {
            it.copy(
                originLat = null,
                originLng = null,
                destLat = null,
                destLng = null,
                calculatedRoutes = emptyList(),
                selectedRoute = null
            )
        }
    }

    fun calculateRoutes() {
        val state = _uiState.value
        val vehicle = state.selectedVehicle ?: return
        val oLat = state.originLat ?: return
        val oLng = state.originLng ?: return
        val dLat = state.destLat ?: return
        val dLng = state.destLng ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, rawErrorMessage = null) }

            try {
                val routes = fetchOSRMAlternatives(oLat, oLng, dLat, dLng, vehicle)
                _uiState.update {
                    it.copy(
                        calculatedRoutes = routes,
                        selectedRoute = routes.firstOrNull(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rawErrorMessage = e.message
                    )
                }
            }
        }
    }

    private suspend fun fetchOSRMAlternatives(
        oLat: Double, oLng: Double,
        dLat: Double, dLng: Double,
        vehicle: Vehicle
    ): List<RouteOption> = withContext(Dispatchers.IO) {
        val urlString = "https://router.project-osrm.org/route/v1/driving/$oLng,$oLat;$dLng,$dLat?overview=full&geometries=geojson&alternatives=true"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "GasTourApp/1.0")
        connection.connectTimeout = 8000
        connection.readTimeout = 8000

        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(responseText)
        val routesArray = json.getJSONArray("routes")

        val resultList = mutableListOf<RouteOption>()

        val safeEfficiency = if (vehicle.efficiencyKmL > 0.0) vehicle.efficiencyKmL else 14.5
        val safeWeight = if (vehicle.weightKg > 0.0) vehicle.weightKg else 1085.0

        for (i in 0 until routesArray.length()) {
            val routeObj = routesArray.getJSONObject(i)
            val distanceMeters = routeObj.getDouble("distance")
            val durationSeconds = routeObj.getDouble("duration")

            val distanceKm = (distanceMeters / 1000.0)
            val durationMin = (durationSeconds / 60.0)

            val geometryObj = routeObj.getJSONObject("geometry")
            val coordinatesArray = geometryObj.getJSONArray("coordinates")
            val geoPoints = mutableListOf<GeoPoint>()

            for (j in 0 until coordinatesArray.length()) {
                val cord = coordinatesArray.getJSONArray(j)
                val lng = cord.getDouble(0)
                val lat = cord.getDouble(1)
                geoPoints.add(GeoPoint(lat, lng))
            }

            val elevationMeters = if (i == 0) 240.0 else 110.0
            val weightFactor = 1.0 + (((safeWeight - 1200.0) / 100.0) * 0.015)
            val elevationFactor = 1.0 + ((elevationMeters / 100.0) * 0.06)

            val fuelLiters = (distanceKm / safeEfficiency) * weightFactor * elevationFactor
            val nameResId = if (i == 0) R.string.route_shortest else R.string.route_efficient

            resultList.add(
                RouteOption(
                    nameResId = nameResId,
                    distanceKm = round(distanceKm * 10.0) / 10.0,
                    durationMin = round(durationMin * 10.0) / 10.0,
                    elevationGainMeters = elevationMeters,
                    fuelLiters = round(fuelLiters * 100.0) / 100.0,
                    pathPoints = geoPoints
                )
            )
        }

        return@withContext resultList
    }

    fun selectRouteOption(route: RouteOption) {
        _uiState.update { it.copy(selectedRoute = route) }
    }

    fun saveSelectedRoute(onSuccess: () -> Unit) {
        val state = _uiState.value
        val route = state.selectedRoute ?: return
        val vehicle = state.selectedVehicle ?: return
        val oLat = state.originLat ?: return
        val oLng = state.originLng ?: return
        val dLat = state.destLat ?: return
        val dLng = state.destLng ?: return

        viewModelScope.launch {
            val originAddress = fetchAddressName(oLat, oLng)
            val destAddress = fetchAddressName(dLat, dLng)

            try {
                val request = SavedRouteCreateRequest(
                    originName = originAddress,
                    destinationName = destAddress,
                    originLat = oLat,
                    originLng = oLng,
                    destLat = dLat,
                    destLng = dLng,
                    vehicleModel = vehicle.model,
                    distanceKm = route.distanceKm,
                    fuelLiters = route.fuelLiters
                )
                val response = apiService.saveRoute(request)
                if (response.isSuccessful) {
                    loadUserData()
                    _uiState.update { it.copy(messageResId = R.string.route_saved_success) }
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(rawErrorMessage = e.message) }
            }
        }
    }

    private suspend fun fetchAddressName(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "GasTourApp/1.0 (Contact: emma@gastour.app)")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val displayName = json.optString("display_name", "")
            if (displayName.isNotBlank()) {
                val parts = displayName.split(",")
                if (parts.isNotEmpty()) {
                    val streetOrPlace = parts[0].trim()
                    val suburbOrCity = if (parts.size > 1) parts[1].trim() else ""
                    return@withContext if (suburbOrCity.isNotBlank() && suburbOrCity != streetOrPlace) {
                        "$streetOrPlace, $suburbOrCity"
                    } else {
                        streetOrPlace
                    }
                }
            }

            val address = json.optJSONObject("address")
            val road = address?.optString("road", "") ?: address?.optString("pedestrian", "") ?: ""
            val suburb = address?.optString("suburb", "") ?: address?.optString("neighbourhood", "") ?: address?.optString("city", "") ?: ""

            when {
                road.isNotBlank() && suburb.isNotBlank() -> "$road, $suburb"
                road.isNotBlank() -> road
                suburb.isNotBlank() -> suburb
                else -> String.format(Locale.ROOT, getApplication<Application>().getString(R.string.location_fallback_format), lat, lng)
            }
        } catch (_: Exception) {
            String.format(Locale.ROOT, getApplication<Application>().getString(R.string.location_fallback_format), lat, lng)
        }
    }

    fun loadSavedRouteToMap(savedRoute: SavedRoute, onReady: () -> Unit) {
        _uiState.update {
            it.copy(
                originLat = savedRoute.originLat,
                originLng = savedRoute.originLng,
                destLat = savedRoute.destLat,
                destLng = savedRoute.destLng
            )
        }

        calculateRoutes()
        onReady()
    }
}