package com.mobile.micasaestucasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobile.micasaestucasa.ui.screens.auth.LoginScreen
import com.mobile.micasaestucasa.ui.screens.auth.RegisterScreen
import com.mobile.micasaestucasa.ui.screens.home.HomeScreen
import com.mobile.micasaestucasa.ui.screens.profile.ProfileScreen
import com.mobile.micasaestucasa.ui.screens.property.PropertyDetailScree
import com.mobile.micasaestucasa.ui.screens.wishlist.WishlistScreen
import com.mobile.micasaestucasa.ui.viewmodels.MainViewModel
@Composable
fun AppNavigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = viewModel.startDestination
    ) {
        composable<Route.Login> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register)
                }
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Login)
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Home> {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSearch = { /* TODO */ },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                },
                onNavigateToTrips = { /* TODO */ },
                onNavigateToSaved = {
                    // Navigazione verso la wishlist corretta
                    navController.navigate(Route.Wishlist)
                }
            )
        }

        composable<Route.Profile> {
            ProfileScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogoutNavigate = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Wishlist> {
            WishlistScreen(
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                }
            )
        }

        composable<Route.PropertyDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PropertyDetail>()
            PropertyDetailScree(
                propertyId = route.propertyId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBooking = { /* TODO */ },
                onNavigateToChat = { /* TODO */ }
            )
        }
    }
}
