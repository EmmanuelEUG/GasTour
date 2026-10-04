package com.emma.proyects.gastour.UI.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.UI.components.MapWebView
import com.emma.proyects.gastour.UI.viewmodel.AppViewModel

@Composable
fun MapScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Aplica el área segura de la pantalla
        ) {
            // Tarjeta superior con info del vehículo activo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Auto: ${state.selectedVehicle?.model ?: "N/A"}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Peso: ${state.selectedVehicle?.weightKg ?: 0.0} kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Contenedor del Mapa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Dentro de MapScreen.kt:
                MapWebView(
                    modifier = Modifier.fillMaxSize(),
                    routes = state.calculatedRoutes,
                    selectedRoute = state.selectedRoute,
                    onPointSelected = { lat, lng, isOrigin ->
                        if (isOrigin) {
                            viewModel.setOrigin(lat, lng)
                        } else {
                            viewModel.setDestination(lat, lng)
                        }
                    }
                )
            }

            // Panel inferior de control y cálculo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when {
                            state.originLat == null -> "Toca el mapa para marcar el ORIGEN (Punto A)"
                            state.destLat == null -> "Toca el mapa para marcar el DESTINO (Punto B)"
                            else -> "Puntos A y B marcados correctamente"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.calculateRoutes() },
                        enabled = state.originLat != null && state.destLat != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isLoading) "Calculando con relieve..." else "Calcular Rutas y Gasolina")
                    }

                    // Resultados de Rutas
                    if (state.calculatedRoutes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Selecciona tu ruta:", style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            state.calculatedRoutes.forEach { route ->
                                FilterChip(
                                    selected = state.selectedRoute == route,
                                    onClick = { viewModel.selectRouteOption(route) },
                                    label = { Text(route.name) }
                                )
                            }
                        }

                        state.selectedRoute?.let { route ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Distancia: ${route.distanceKm} km | Tiempo: ${route.durationMin} min")
                            Text("Elevación/Relieve: +${route.elevationGainMeters} m")
                            Text(
                                text = "Gasolina estimada: ${String.format("%.2f", route.fuelLiters)} Litros",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.saveSelectedRoute {} },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Guardar esta Ruta")
                            }
                        }
                    }

                    state.message?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}