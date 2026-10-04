package com.emma.proyects.gastour.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectionScreen(
    viewModel: AppViewModel,
    onContinueToMap: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    var newModel by remember { mutableStateOf("") }
    var newEff by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.select_vehicle_title),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.selectedVehicle?.model ?: stringResource(id = R.string.select_vehicle_placeholder),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.userVehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        id = R.string.vehicle_dropdown_format,
                                        vehicle.model,
                                        vehicle.efficiencyKmL,
                                        vehicle.weightKg
                                    )
                                )
                            },
                            onClick = {
                                viewModel.selectVehicle(vehicle)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.add_vehicle_prompt),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newModel,
                        onValueChange = { newModel = it },
                        label = { Text(stringResource(id = R.string.model_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEff,
                        onValueChange = { newEff = it },
                        label = { Text(stringResource(id = R.string.efficiency_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text(stringResource(id = R.string.weight_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val eff = newEff.toDoubleOrNull() ?: 12.0
                            val w = newWeight.toDoubleOrNull() ?: 1200.0
                            if (newModel.isNotBlank()) {
                                viewModel.addVehicle(newModel, eff, w)
                                newModel = ""
                                newEff = ""
                                newWeight = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(id = R.string.save_vehicle_btn))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinueToMap,
                enabled = state.selectedVehicle != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.go_to_map_btn))
            }
        }
    }
}