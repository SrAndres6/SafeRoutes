package com.example.jetpack_compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpack_compose.SafeRouteApplication
import com.example.jetpack_compose.data.remote.NetworkModule
import com.example.jetpack_compose.ui.viewmodel.AuthViewModel
import com.example.jetpack_compose.ui.viewmodel.HomeViewModel
import com.example.jetpack_compose.ui.viewmodel.MapViewModel
import com.example.jetpack_compose.ui.viewmodel.ProfileViewModel
import com.example.jetpack_compose.ui.viewmodel.ReportViewModel
import com.example.jetpack_compose.ui.viewmodel.RouteDetailViewModel
import com.example.jetpack_compose.ui.viewmodel.RouteListViewModel
import com.example.jetpack_compose.util.Constants

@Composable
fun authViewModel(): AuthViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = AuthViewModel.Factory(app.authRepository))
}

@Composable
fun homeViewModel(): HomeViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = HomeViewModel.Factory(app.authRepository, app.routeRepository))
}

@Composable
fun mapViewModel(): MapViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(
        factory = MapViewModel.Factory(
            NetworkModule.directionsService,
            app.routeRepository,
            app.zoneRepository,
            app.authRepository,
            Constants.GOOGLE_MAPS_API_KEY
        )
    )
}

@Composable
fun routeListViewModel(): RouteListViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = RouteListViewModel.Factory(app.routeRepository))
}

@Composable
fun routeDetailViewModel(routeId: String): RouteDetailViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = RouteDetailViewModel.Factory(
        routeId, app.routeRepository, app.reportRepository, app.authRepository
    ))
}

@Composable
fun reportViewModel(): ReportViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = ReportViewModel.Factory(
        app.reportRepository, app.routeRepository, app.authRepository
    ))
}

@Composable
fun profileViewModel(): ProfileViewModel {
    val app = LocalContext.current.applicationContext as SafeRouteApplication
    return viewModel(factory = ProfileViewModel.Factory(app.authRepository, app.routeRepository))
}
