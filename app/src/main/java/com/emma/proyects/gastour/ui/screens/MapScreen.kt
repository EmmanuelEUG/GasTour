package com.emma.proyects.gastour.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.ui.components.MapWebView
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

@Composable
fun MapScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        text = stringResource(
                            id = R.string.vehicle_prefix,
                            state.selectedVehicle?.model ?: "N/A"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(
                            id = R.string.weight_prefix,
                            state.selectedVehicle?.weightKg ?: 0.0
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when {
                            state.originLat == null -> stringResource(id = R.string.mark_origin_prompt)
                            state.destLat == null -> stringResource(id = R.string.mark_dest_prompt)
                            else -> stringResource(id = R.string.points_marked_prompt)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.calculateRoutes() },
                        enabled = state.originLat != null && state.destLat != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (state.isLoading)
                                stringResource(id = R.string.calculating_btn)
                            else
                                stringResource(id = R.string.calculate_routes_btn)
                        )
                    }

                    if (state.calculatedRoutes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.select_route_label),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            state.calculatedRoutes.forEach { route ->
                                FilterChip(
                                    selected = state.selectedRoute == route,
                                    onClick = { viewModel.selectRouteOption(route) },
                                    label = { Text(stringResource(id = route.nameResId)) }
                                )
                            }
                        }

                        state.selectedRoute?.let { route ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                stringResource(
                                    id = R.string.distance_time_format,
                                    route.distanceKm,
                                    route.durationMin
                                )
                            )
                            Text(
                                stringResource(
                                    id = R.string.elevation_format,
                                    route.elevationGainMeters
                                )
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.fuel_estimate_format,
                                    route.fuelLiters
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.saveSelectedRoute {} },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(stringResource(id = R.string.save_route_btn))
                            }
                        }
                    }

                    state.messageResId?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(id = it), color = MaterialTheme.colorScheme.primary)
                    }
                    state.rawErrorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}