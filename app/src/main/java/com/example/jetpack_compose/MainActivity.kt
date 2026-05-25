package com.example.jetpack_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jetpack_compose.ui.screen.*
import com.example.jetpack_compose.ui.theme.Jetpack_composeTheme

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToMap = { navController.navigate(MapRoute) },
                onNavigateToReport = { navController.navigate(ReportRoute) },
                onNavigateToEmergency = { navController.navigate(EmergencyRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable<MapRoute> {
            // Placeholder del Mapa (Integración de Google Maps SDK requerida)
            DetailScreen(
                itemId = "Mapa",
                title = "Navegación Segura",
                onBack = { navController.popBackStack() }
            )
        }

        composable<ReportRoute> {
            ReportScreen(onBack = { navController.popBackStack() })
        }

        composable<EmergencyRoute> {
            // Acción SOS inmediata
            Toast.makeText(context, "¡ALERTA SOS ENVIADA!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Jetpack_composeTheme {
                AppNavigation()
            }
        }
    }
}
