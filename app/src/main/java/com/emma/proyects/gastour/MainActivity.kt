package com.emma.proyects.gastour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emma.proyects.gastour.ui.screens.AccountScreen
import com.emma.proyects.gastour.ui.screens.LoginScreen
import com.emma.proyects.gastour.ui.screens.MapScreen
import com.emma.proyects.gastour.ui.screens.RegisterScreen
import com.emma.proyects.gastour.ui.screens.VehicleSelectionScreen
import com.emma.proyects.gastour.ui.theme.GasTourTheme
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GasTourTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GasTourNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GasTourNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("register") {
            RegisterScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("vehicle_selection") {
            VehicleSelectionScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("map") {
            MapScreen(
                viewModel = viewModel,
                onNavigateToAccount = {
                    navController.navigate("account")
                }
            )
        }

        composable("account") {
            AccountScreen(
                viewModel = viewModel,
                onBackToMap = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}