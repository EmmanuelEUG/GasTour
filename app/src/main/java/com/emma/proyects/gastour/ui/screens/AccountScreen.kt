package com.emma.proyects.gastour.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.Vehicle
import com.emma.proyects.gastour.data.network.VehicleCatalogDto
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AppViewModel,
    onBackToMap: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackToMap) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.account_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout {
                            onLogout()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.account_logout)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val username = uiState.currentUser?.username ?: stringResource(R.string.account_loading)
                        Text(
                            text = stringResource(R.string.account_user_label, username),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row {
                            IconButton(onClick = { showEditUsernameDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.account_edit_name)
                                )
                            }
                            IconButton(onClick = { showChangePasswordDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = stringResource(R.string.account_change_pass)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.account_vehicles_title),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { showAddVehicleDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.account_add_vehicle)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.userVehicles) { vehicle ->
                    val isSelected = uiState.selectedVehicle?.id == vehicle.id
                    Card(
                        onClick = { viewModel.selectVehicle(vehicle) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vehicle.model,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(
                                        R.string.vehicle_specs_format,
                                        vehicle.efficiencyKmL,
                                        vehicle.weightKg
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row {
                                IconButton(onClick = { vehicleToEdit = vehicle }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.account_edit_vehicle)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteVehicle(vehicle.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.account_delete)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.account_routes_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.savedRoutes) { route ->
                    Card(
                        onClick = {
                            viewModel.loadSavedRouteToMap(route) {
                                onBackToMap()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${route.originName} ➔ ${route.destinationName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(
                                        R.string.route_specs_format,
                                        route.distanceKm,
                                        route.fuelLiters
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = stringResource(R.string.account_view_on_map)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddVehicleDialog) {
        AddVehicleDialog(
            viewModel = viewModel,
            onDismiss = { showAddVehicleDialog = false }
        )
    }

    vehicleToEdit?.let { vehicle ->
        EditVehicleDialog(
            vehicle = vehicle,
            onConfirm = { updatedModel, updatedEfficiency, updatedWeight ->
                viewModel.updateVehicle(vehicle.id, updatedModel, updatedEfficiency, updatedWeight)
                vehicleToEdit = null
            },
            onDismiss = { vehicleToEdit = null }
        )
    }

    if (showEditUsernameDialog) {
        var newUsername by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showEditUsernameDialog = false },
            title = { Text(stringResource(R.string.dialog_update_user_title)) },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text(stringResource(R.string.dialog_new_user_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUsername.isNotBlank()) {
                            viewModel.updateUsername(newUsername) {
                                showEditUsernameDialog = false
                                onLogout()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsernameDialog = false }) {
                    Text(stringResource(R.string.dialog_btn_cancel))
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text(stringResource(R.string.dialog_change_pass_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(stringResource(R.string.dialog_current_pass_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.dialog_new_pass_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                            viewModel.changePassword(currentPassword, newPassword) {
                                showChangePasswordDialog = false
                                onLogout()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_btn_change))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text(stringResource(R.string.dialog_btn_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var catalogList by remember { mutableStateOf<List<VehicleCatalogDto>>(emptyList()) }
    var selectedCatalogItem by remember { mutableStateOf<VehicleCatalogDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var modelName by remember { mutableStateOf("") }
    var efficiencyText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadVehicleCatalog { list ->
            catalogList = list
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_vehicle_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCatalogItem?.let { "${it.brand} ${it.model}" }
                            ?: stringResource(R.string.dialog_select_catalog),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        catalogList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.brand} ${item.model} (${item.year})") },
                                onClick = {
                                    selectedCatalogItem = item
                                    modelName = "${item.brand} ${item.model}"
                                    efficiencyText = item.cityConsumption.toString()
                                    weightText = item.weightKg.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text(stringResource(R.string.dialog_vehicle_model_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = efficiencyText,
                    onValueChange = { efficiencyText = it },
                    label = { Text(stringResource(R.string.dialog_efficiency_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.dialog_weight_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val efficiency = efficiencyText.toDoubleOrNull() ?: 14.0
                    val weight = weightText.toDoubleOrNull() ?: 1100.0

                    if (modelName.isNotBlank()) {
                        viewModel.addVehicle(modelName, efficiency, weight)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_btn_cancel))
            }
        }
    )
}

@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    onConfirm: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var modelName by remember { mutableStateOf(vehicle.model) }
    var efficiencyText by remember { mutableStateOf(vehicle.efficiencyKmL.toString()) }
    var weightText by remember { mutableStateOf(vehicle.weightKg.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_edit_vehicle_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text(stringResource(R.string.dialog_vehicle_model_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = efficiencyText,
                    onValueChange = { efficiencyText = it },
                    label = { Text(stringResource(R.string.dialog_efficiency_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.dialog_weight_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val efficiency = efficiencyText.toDoubleOrNull() ?: vehicle.efficiencyKmL
                    val weight = weightText.toDoubleOrNull() ?: vehicle.weightKg
                    if (modelName.isNotBlank()) {
                        onConfirm(modelName, efficiency, weight)
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_btn_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_btn_cancel))
            }
        }
    )
}