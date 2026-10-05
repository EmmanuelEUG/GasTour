package com.emma.proyects.gastour.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emma.proyects.gastour.data.network.VehicleCatalogDto
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectionScreen(
    navController: NavController,
    viewModel: AppViewModel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Selecciona o Registra un Vehículo",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCatalogItem?.let { "${it.brand} ${it.model} (${it.year})" } ?: "Seleccionar del catálogo",
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
                catalogList.forEach { vehicle ->
                    DropdownMenuItem(
                        text = { Text("${vehicle.brand} ${vehicle.model} (${vehicle.year})") },
                        onClick = {
                            selectedCatalogItem = vehicle
                            modelName = "${vehicle.brand} ${vehicle.model}"
                            efficiencyText = vehicle.cityConsumption.toString()
                            weightText = vehicle.weightKg.toString()
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = { modelName = it },
            label = { Text("Modelo del vehículo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = efficiencyText,
            onValueChange = { efficiencyText = it },
            label = { Text("Rendimiento (km/L)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val efficiency = efficiencyText.toDoubleOrNull() ?: 12.0
                val weight = weightText.toDoubleOrNull() ?: 1200.0

                if (modelName.isNotBlank()) {
                    viewModel.addVehicle(modelName, efficiency, weight)
                    navController.navigate("map") {
                        popUpTo("vehicle_selection") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y Continuar al Mapa")
        }
    }
}