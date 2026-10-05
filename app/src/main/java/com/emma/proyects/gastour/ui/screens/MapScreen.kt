package com.emma.proyects.gastour.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.ui.components.MapWebView
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: AppViewModel,
    onNavigateToAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var showVehicleMenu by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun fetchDeviceLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            fetchDeviceLocation()
        }
    }

    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            fetchDeviceLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val originPoint = if (state.originLat != null && state.originLng != null) {
        GeoPoint(state.originLat!!, state.originLng!!)
    } else null

    val destPoint = if (state.destLat != null && state.destLng != null) {
        GeoPoint(state.destLat!!, state.destLng!!)
    } else null

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 72.dp,
        sheetSwipeEnabled = true,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = when {
                        state.originLat == null -> stringResource(id = R.string.mark_origin_prompt)
                        state.destLat == null -> stringResource(id = R.string.mark_dest_prompt)
                        else -> stringResource(id = R.string.points_marked_prompt)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.calculateRoutes() },
                        enabled = state.originLat != null && state.destLat != null && !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (state.isLoading)
                                stringResource(id = R.string.calculating_btn)
                            else
                                stringResource(id = R.string.calculate_routes_btn),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (state.originLat != null || state.destLat != null) {
                        OutlinedIconButton(onClick = { viewModel.clearPoints() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = state.calculatedRoutes.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = stringResource(id = R.string.select_route_label),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.calculatedRoutes.forEach { route ->
                                val isSelected = state.selectedRoute == route
                                if (isSelected) {
                                    Button(
                                        onClick = { viewModel.selectRouteOption(route) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stringResource(id = route.nameResId),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.selectRouteOption(route) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stringResource(id = route.nameResId),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        state.selectedRoute?.let { route ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.distance_time_format,
                                        route.distanceKm,
                                        route.durationMin
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(
                                        id = R.string.elevation_format,
                                        route.elevationGainMeters
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.fuel_estimate_format,
                                            route.fuelLiters
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

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
                }

                state.messageResId?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(id = it), color = MaterialTheme.colorScheme.primary)
                }
                state.rawErrorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToAccount) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(id = R.string.my_account_title)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapWebView(
                modifier = Modifier.fillMaxSize(),
                routes = state.calculatedRoutes,
                selectedRoute = state.selectedRoute,
                originPoint = originPoint,
                destPoint = destPoint,
                initialCenter = userLocation,
                onPointSelected = { lat, lng, isOrigin ->
                    if (isOrigin) {
                        viewModel.setOrigin(lat, lng)
                    } else {
                        viewModel.setDestination(lat, lng)
                    }
                }
            )

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
            ) {
                Surface(
                    modifier = Modifier.clickable { showVehicleMenu = true },
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = state.selectedVehicle?.model ?: "N/A",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.weight_prefix,
                                    state.selectedVehicle?.weightKg ?: 0.0
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showVehicleMenu,
                    onDismissRequest = { showVehicleMenu = false }
                ) {
                    state.userVehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = vehicle.model,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${vehicle.efficiencyKmL} km/L • ${vehicle.weightKg} kg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectVehicle(vehicle)
                                showVehicleMenu = false
                                if (state.originLat != null && state.destLat != null) {
                                    viewModel.calculateRoutes()
                                }
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { fetchDeviceLocation() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(id = R.string.my_location_btn)
                )
            }
        }
    }
}