package de.codevoid.gpxmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.codevoid.gpxmanager.ui.location.LocationDetailScreen
import de.codevoid.gpxmanager.ui.location.LocationLibraryScreen
import de.codevoid.gpxmanager.ui.main.MainScreen
import de.codevoid.gpxmanager.ui.settings.SettingsScreen
import de.codevoid.gpxmanager.ui.trip.TripLibraryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToTrips = {
                    navController.navigate(Screen.TripLibrary.createRoute())
                },
                onNavigateToLocations = {
                    navController.navigate(Screen.LocationLibrary.createRoute())
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.TripLibrary.route,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderIdStr = backStackEntry.arguments?.getString("folderId")
            val folderId = folderIdStr?.toLongOrNull()
            TripLibraryScreen(
                folderId = folderId,
                onNavigateToFolder = { id ->
                    navController.navigate(Screen.TripLibrary.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LocationLibrary.route,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderIdStr = backStackEntry.arguments?.getString("folderId")
            val folderId = folderIdStr?.toLongOrNull()
            LocationLibraryScreen(
                folderId = folderId,
                onNavigateToFolder = { id ->
                    navController.navigate(Screen.LocationLibrary.createRoute(id))
                },
                onNavigateToLocation = { id ->
                    navController.navigate(Screen.LocationDetail.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LocationDetail.route,
            arguments = listOf(navArgument("locationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getLong("locationId") ?: return@composable
            LocationDetailScreen(
                locationId = locationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
