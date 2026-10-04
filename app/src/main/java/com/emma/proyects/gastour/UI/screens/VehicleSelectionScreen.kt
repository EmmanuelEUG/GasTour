package com.emma.proyects.gastour.UI.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emma.proyects.gastour.UI.viewmodel.AppViewModel


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selecciona tu Vehículo", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // DROPDOWN / SELECT DE VEHÍCULOS
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.selectedVehicle?.model ?: "Selecciona un auto...",
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
                        text = { Text("${vehicle.model} (${vehicle.efficiencyKmL} km/L, ${vehicle.weightKg} kg)") },
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
                Text("¿No ves tu auto? Agrégalo:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newModel,
                    onValueChange = { newModel = it },
                    label = { Text("Modelo del auto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newEff,
                    onValueChange = { newEff = it },
                    label = { Text("Rendimiento (km/L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newWeight,
                    onValueChange = { newWeight = it },
                    label = { Text("Peso del auto (kg)") },
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
                    Text("Guardar Auto")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinueToMap,
            enabled = state.selectedVehicle != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir al Mapa")
        }
    }
}