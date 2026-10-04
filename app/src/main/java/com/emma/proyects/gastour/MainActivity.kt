package com.emma.proyects.gastour

import com.emma.proyects.gastour.UI.screens.LoginScreen
import com.emma.proyects.gastour.UI.screens.MapScreen
import com.emma.proyects.gastour.UI.screens.RegisterScreen
import com.emma.proyects.gastour.UI.screens.VehicleSelectionScreen
import com.emma.proyects.gastour.UI.viewmodel.AppViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar configuración de osmdroid para manejo de caché de mapas
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        setContent {
            val navController = rememberNavController()
            val mainViewModel: AppViewModel = viewModel()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        viewModel = mainViewModel,
                        onNavigateToRegister = { navController.navigate("register") },
                        onLoginSuccess = { navController.navigate("vehicles") }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        viewModel = mainViewModel,
                        onRegisterSuccess = { navController.navigate("vehicles") },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
                composable("vehicles") {
                    VehicleSelectionScreen(
                        viewModel = mainViewModel,
                        onContinueToMap = { navController.navigate("map") }
                    )
                }
                composable("map") {
                    MapScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}