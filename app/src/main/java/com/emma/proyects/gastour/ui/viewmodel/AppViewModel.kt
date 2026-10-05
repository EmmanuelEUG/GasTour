package com.emma.proyects.gastour.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.RouteOption
import com.emma.proyects.gastour.data.models.SavedRoute
import com.emma.proyects.gastour.data.models.User
import com.emma.proyects.gastour.data.models.Vehicle
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun login(user: String, pass: String, onSuccess: () -> Unit) {
        if (user.isNotBlank() && pass.isNotBlank()) {
            _uiState.update {
                it.copy(
                    currentUser = User(1, user),
                    userVehicles = listOf(
                        Vehicle(1, "Nissan Versa (Sedan)", 15.0, 1100.0),
                        Vehicle(2, "Chevrolet Pickup (Truck)", 9.0, 1800.0),
                        Vehicle(3, "VW Golf (Hatchback)", 14.0, 1250.0)
                    )
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(messageResId = R.string.auth_error_fields) }
        }
    }

    fun register(user: String, pass: String, onSuccess: () -> Unit) {
        if (user.isNotBlank() && pass.isNotBlank()) {
            _uiState.update {
                it.copy(
                    currentUser = User(1, user),
                    userVehicles = listOf(Vehicle(1, "Nissan Versa (Sedan)", 15.0, 1100.0))
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(messageResId = R.string.auth_error_all_fields) }
        }
    }

    fun addVehicle(model: String, efficiency: Double, weight: Double) {
        val currentList = _uiState.value.userVehicles.toMutableList()
        val newVehicle = Vehicle(currentList.size + 1, model, efficiency, weight)
        currentList.add(newVehicle)
        _uiState.update { it.copy(userVehicles = currentList, selectedVehicle = newVehicle) }
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
            val weightFactor = 1.0 + (((vehicle.weightKg - 1200.0) / 100.0) * 0.015)
            val elevationFactor = 1.0 + ((elevationMeters / 100.0) * 0.06)

            val fuelLiters = (distanceKm / vehicle.efficiencyKmL) * weightFactor * elevationFactor
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



    private suspend fun fetchAddressName(lat: Double, lng: Double, defaultFallback: String): String = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=16"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "GasTourApp/1.0")
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            val address = json.optJSONObject("address")

            val road = address?.optString("road", "") ?: ""
            val suburb = address?.optString("suburb", "") ?: address?.optString("neighbourhood", "") ?: ""

            when {
                road.isNotBlank() && suburb.isNotBlank() -> "$road, $suburb"
                road.isNotBlank() -> road
                else -> defaultFallback
            }
        } catch (_: Exception) {
            defaultFallback
        }
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
            // Obtener nombres reales de las calles por geocoding
            val originAddress = fetchAddressName(oLat, oLng, "Origen ($oLat, $oLng)")
            val destAddress = fetchAddressName(dLat, dLng, "Destino ($dLat, $dLng)")
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val newSavedRoute = SavedRoute(
                id = state.savedRoutes.size + 1,
                originName = originAddress,
                destinationName = destAddress,
                originLat = oLat,
                originLng = oLng,
                destLat = dLat,
                destLng = dLng,
                vehicleModel = vehicle.model,
                distanceKm = route.distanceKm,
                fuelLiters = route.fuelLiters,
                date = currentDate
            )

            _uiState.update {
                it.copy(
                    savedRoutes = it.savedRoutes + newSavedRoute,
                    messageResId = R.string.route_saved_success
                )
            }
            onSuccess()
        }
    }

    fun loadSavedRouteToMap(savedRoute: SavedRoute, onReady: () -> Unit) {
        // Asignar dinámicamente las coordenadas exactas almacenadas en la ruta seleccionada
        _uiState.update {
            it.copy(
                originLat = savedRoute.originLat,
                originLng = savedRoute.originLng,
                destLat = savedRoute.destLat,
                destLng = savedRoute.destLng
            )
        }

        // Trazar automáticamente las rutas y re-centrar el mapa en la pantalla
        calculateRoutes()
        onReady()
    }
}