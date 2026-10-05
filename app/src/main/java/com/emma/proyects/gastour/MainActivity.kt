package com.emma.proyects.gastour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emma.proyects.gastour.ui.screens.*
import com.emma.proyects.gastour.ui.theme.GasTourTheme
import com.emma.proyects.gastour.ui.viewmodel.AppViewModel
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        )

        setContent {
            GasTourTheme {
                val navController = rememberNavController()
                val mainViewModel: AppViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = mainViewModel,
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = { navController.navigate("vehicle_selection") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            viewModel = mainViewModel,
                            onRegisterSuccess = { navController.navigate("vehicle_selection") },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }
                    composable("vehicle_selection") {
                        VehicleSelectionScreen(
                            viewModel = mainViewModel,
                            onContinueToMap = { navController.navigate("map") }
                        )
                    }
                    composable("map") {
                        MapScreen(
                            viewModel = mainViewModel,
                            onNavigateToAccount = { navController.navigate("account") }
                        )
                    }
                    composable("account") {
                        AccountScreen(
                            viewModel = mainViewModel,
                            onBackToMap = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
