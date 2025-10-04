package net.calvuz.quickstore.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.calvuz.quickstore.presentation.ui.camera.CameraScreen
import net.calvuz.quickstore.presentation.ui.camera.SearchResultsScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object Camera : Screen("camera")
    data object SearchResults : Screen("search_results/{articleUuids}") {
        fun createRoute(articleUuids: List<String>): String {
            return "search_results/${articleUuids.joinToString(",")}"
        }
    }
}

/**
 * App Navigation Host
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {
        // Camera Screen
        composable(Screen.Camera.route) {
            CameraScreen(
                onSearchResults = { articleUuids ->
                    if (articleUuids.isNotEmpty()) {
                        val route = Screen.SearchResults.createRoute(articleUuids)
                        navController.navigate(route)
                    }
                },
                onBack = {
                    // Se hai altre schermate, naviga indietro
                    // Altrimenti puoi chiudere l'app o tornare alla home
                    navController.popBackStack()
                }
            )
        }

        // Search Results Screen
        composable(
            route = Screen.SearchResults.route,
            arguments = listOf(
                navArgument("articleUuids") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uuidsString = backStackEntry.arguments?.getString("articleUuids") ?: ""
            val articleUuids = uuidsString.split(",").filter { it.isNotBlank() }

            SearchResultsScreen(
                articleUuids = articleUuids,
                onArticleClick = { articleUuid ->
                    // TODO: Naviga a dettaglio articolo
                    // navController.navigate("article_detail/$articleUuid")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: Aggiungi altre schermate qui
        // - Home
        // - Article List
        // - Article Detail
        // - Add Article
        // - Movements List
        // etc.
    }
}