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
data object MapRoute // Pantalla principal de navegación segura

@Serializable
data object ReportRoute // Para reportar incidentes (Crowdsourcing)

@Serializable
data object EmergencyRoute // Función "Acompáñame" / SOS

@Serializable
data object FriendsRoute
