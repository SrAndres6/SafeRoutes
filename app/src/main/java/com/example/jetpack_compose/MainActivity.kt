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
import androidx.navigation.toRoute
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpack_compose.ui.screen.*
import com.example.jetpack_compose.ui.theme.Jetpack_composeTheme
import com.example.jetpack_compose.worker.SyncReportWorker
import com.google.firebase.FirebaseApp

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) }
            )
        }

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

        composable<HomeRoute> {
            HomeScreen(
                onNavigateToMap = { navController.navigate(MapRoute()) },
                onNavigateToReport = { navController.navigate(ReportRoute) },
                onNavigateToEmergency = { navController.navigate(EmergencyRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) },
                onNavigateToRoutes = { navController.navigate(RouteListRoute) },
                onNavigateToRouteDetail = { routeId ->
                    navController.navigate(RouteDetailNav(routeId))
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                },
                onRouteClick = { routeId ->
                    navController.navigate(RouteDetailNav(routeId))
                }
            )
        }

        composable<MapRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<MapRoute>()
            MapScreen(
                routeId = args.routeId,
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

        composable<RouteListRoute> {
            RouteListScreen(
                onBack = { navController.popBackStack() },
                onRouteClick = { routeId ->
                    navController.navigate(RouteDetailNav(routeId))
                },
                onNavigateToMap = { routeId ->
                    navController.navigate(MapRoute(routeId))
                }
            )
        }

        composable<RouteDetailNav> { backStackEntry ->
            val args = backStackEntry.toRoute<RouteDetailNav>()
            RouteDetailScreen(
                routeId = args.routeId,
                onBack = { navController.popBackStack() }
            )
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

        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequestBuilder<SyncReportWorker>().build()
        )

        setContent {
            Jetpack_composeTheme {
                AppNavigation()
            }
        }
    }
}
