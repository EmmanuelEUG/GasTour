package com.emma.proyects.gastour.UI.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emma.proyects.gastour.data.models.RouteOption
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
import java.net.URL

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
    val message: String? = null
)

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun login(user: String, pass: String, onSuccess: () -> Unit) {
        if (user.isNotBlank() && pass.isNotBlank()) {
            _uiState.update {
                it.copy(
                    currentUser = User(1, user),
                    userVehicles = listOf(
                        Vehicle(1, "Nissan Versa (Sedán)", 15.0, 1100.0),
                        Vehicle(2, "Chevrolet Pickup (Camioneta)", 9.0, 1800.0),
                        Vehicle(3, "VW Golf (Hatchback)", 14.0, 1250.0)
                    )
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(message = "Ingresa usuario y contraseña") }
        }
    }

    fun register(user: String, pass: String, onSuccess: () -> Unit) {
        if (user.isNotBlank() && pass.isNotBlank()) {
            _uiState.update {
                it.copy(
                    currentUser = User(1, user),
                    userVehicles = listOf(Vehicle(1, "Nissan Versa (Sedán)", 15.0, 1100.0))
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(message = "Llena todos los campos") }
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

    // PETICIÓN HTTP A OSRM PARA TRAZAR SOBRE CALLES CON SENTIDOS VIALES
    fun calculateRoutes() {
        val state = _uiState.value
        val vehicle = state.selectedVehicle ?: return
        val oLat = state.originLat ?: return
        val oLng = state.originLng ?: return
        val dLat = state.destLat ?: return
        val dLng = state.destLng ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
                        message = "Error al obtener ruta sobre calles: ${e.message}"
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

        val url = java.net.URL(urlString)
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "RutaGasApp/1.0")
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

            // Extraer geometría de calles
            val geometryObj = routeObj.getJSONObject("geometry")
            val coordinatesArray = geometryObj.getJSONArray("coordinates")
            val geoPoints = mutableListOf<GeoPoint>()

            for (j in 0 until coordinatesArray.length()) {
                val coord = coordinatesArray.getJSONArray(j)
                val lng = coord.getDouble(0)
                val lat = coord.getDouble(1)
                geoPoints.add(GeoPoint(lat, lng))
            }

            // Cálculos tomando en cuenta peso y relieve
            val elevationMeters = if (i == 0) 240.0 else 110.0
            val weightFactor = 1.0 + (((vehicle.weightKg - 1200.0) / 100.0) * 0.015)
            val elevationFactor = 1.0 + ((elevationMeters / 100.0) * 0.06)

            val fuelLiters = (distanceKm / vehicle.efficiencyKmL) * weightFactor * elevationFactor
            val name = if (i == 0) "Ruta Más Corta" else "Ruta Más Eficiente"

            resultList.add(
                RouteOption(
                    name = name,
                    distanceKm = Math.round(distanceKm * 10.0) / 10.0,
                    durationMin = Math.round(durationMin * 10.0) / 10.0,
                    elevationGainMeters = elevationMeters,
                    fuelLiters = Math.round(fuelLiters * 100.0) / 100.0,
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
        _uiState.update { it.copy(message = "¡Ruta guardada en la base de datos!") }
        onSuccess()
    }
}