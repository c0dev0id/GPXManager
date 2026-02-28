package de.codevoid.gpxmanager.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")

    data object TripLibrary : Screen("trip_library/{folderId}") {
        fun createRoute(folderId: Long? = null) = "trip_library/${folderId ?: "root"}"
    }

    data object LocationLibrary : Screen("location_library/{folderId}") {
        fun createRoute(folderId: Long? = null) = "location_library/${folderId ?: "root"}"
    }

    data object LocationDetail : Screen("location_detail/{locationId}") {
        fun createRoute(locationId: Long) = "location_detail/$locationId"
    }

    data object Settings : Screen("settings")
}
