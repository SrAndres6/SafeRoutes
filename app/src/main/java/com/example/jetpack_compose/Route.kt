package com.example.jetpack_compose

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object HomeRoute

@Serializable
data object ProfileRoute

@Serializable
data class MapRoute(val routeId: String? = null)

@Serializable
data object ReportRoute

@Serializable
data object EmergencyRoute

@Serializable
data object FriendsRoute

@Serializable
data object RouteListRoute

@Serializable
data class RouteDetailNav(val routeId: String)
