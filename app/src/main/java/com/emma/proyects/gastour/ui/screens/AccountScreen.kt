package com.emma.proyects.gastour.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.SavedRoute
import com.emma.proyects.gastour.data.models.Vehicle
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AppViewModel,
    onBackToMap: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.my_account_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackToMap) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_btn)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.user_info_section),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${stringResource(id = R.string.user_label)}: ${state.currentUser?.username ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.my_vehicles_section),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (state.userVehicles.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.no_vehicles_msg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.userVehicles) { vehicle ->
                    VehicleItemCard(vehicle = vehicle)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.saved_routes_section),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (state.savedRoutes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.no_saved_routes_msg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.savedRoutes) { route ->
                    SavedRouteItemCard(
                        route = route,
                        onClick = {
                            viewModel.loadSavedRouteToMap(route) {
                                onBackToMap()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleItemCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = vehicle.model, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${vehicle.efficiencyKmL} km/L • ${vehicle.weightKg} kg",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SavedRouteItemCard(
    route: SavedRoute,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.vehicleModel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = route.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "From: ${route.originName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To: ${route.destinationName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = "${route.distanceKm} km • ${route.fuelLiters} Liters",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}