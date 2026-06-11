package com.example.jetpack_compose.data.remote

data class DirectionsResponse(
    val status: String,
    val routes: List<Route>,
    val error_message: String? = null
)

data class Route(
    val overview_polyline: Polyline,
    val legs: List<Leg>
)

data class Polyline(
    val points: String
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val steps: List<Step>
)

data class Distance(val text: String, val value: Int)
data class Duration(val text: String, val value: Int)

data class Step(
    val html_instructions: String,
    val distance: Distance,
    val duration: Duration,
    val start_location: Location,
    val end_location: Location,
    val polyline: Polyline
)

data class Location(val lat: Double, val lng: Double)
