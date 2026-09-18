package com.gopaint.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gopaint.app.data.AuthRepository
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.ui.screens.FavoritesScreen
import com.gopaint.app.ui.screens.HomeScreen
import com.gopaint.app.ui.screens.ItemDetailScreen
import com.gopaint.app.ui.screens.LoginScreen
import com.gopaint.app.ui.screens.UploadScreen

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val UPLOAD = "upload"
    const val FAVORITES = "favorites"
    const val DETAIL = "detail"
}

/** Kept in-memory only: the currently opened item, set right before navigating to detail. */
private var currentDetailItem: ContentItem? = null

@Composable
fun GoPaintNavGraph(
    authRepository: AuthRepository,
    contentRepository: ContentRepository
) {
    val navController = rememberNavController()
    val startDestination = remember { if (authRepository.isLoggedIn()) Routes.HOME else Routes.LOGIN }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = authRepository,
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                repository = contentRepository,
                onItemClick = { item ->
                    currentDetailItem = item
                    navController.navigate(Routes.DETAIL)
                },
                onUploadClick = { navController.navigate(Routes.UPLOAD) },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(Routes.DETAIL) {
            currentDetailItem?.let { item ->
                ItemDetailScreen(
                    item = item,
                    repository = contentRepository,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.UPLOAD) {
            UploadScreen(
                repository = contentRepository,
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                repository = contentRepository,
                onItemClick = { item ->
                    currentDetailItem = item
                    navController.navigate(Routes.DETAIL)
                }
            )
        }
    }
}
