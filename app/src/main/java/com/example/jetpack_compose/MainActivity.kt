package com.example.jetpack_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
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
        startDestination = LoginRoute // Empezamos por el Login
    ) {
        // Pantalla de Login
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true } // Limpiar historial
                    }
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) }
            )
        }

        // Pantalla de Registro
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = { 
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Pantalla Principal (Dashboard)
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
            MapScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFriends = { navController.navigate(FriendsRoute) }
            )
        }

        composable<FriendsRoute> {
            FriendsScreen(onBack = { navController.popBackStack() })
        }

        composable<ReportRoute> {
            ReportScreen(onBack = { navController.popBackStack() })
        }

        composable<EmergencyRoute> {
            Toast.makeText(context, "¡ALERTA SOS ENVIADA!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            Jetpack_composeTheme {
                AppNavigation()
            }
        }
    }
}
