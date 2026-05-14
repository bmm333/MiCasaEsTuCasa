package com.mobile.micasaestucasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.ui.screens.auth.LoginScreen
import com.mobile.micasaestucasa.ui.screens.auth.RegisterScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingListScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingRequestScreen
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
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
                onNavigateToTrips = {
                    navController.navigate(Route.BookingList)
                },
                onNavigateToSaved = { /* TODO */ }
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
                onNavigateToBooking = { propId, title, price, hostId ->
                    navController.navigate(
                        Route.BookingRequest(
                            propertyId = propId,
                            propertyTitle = title,
                            pricePerDay = price,
                            hostId = hostId
                        )
                    )
                },
                onNavigateToChat = { /* TODO */ }
            )
        }

        composable<Route.BookingRequest> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.BookingRequest>()
            BookingRequestScreen(
                propertyId = route.propertyId,
                propertyTitle = route.propertyTitle,
                pricePerDay = route.pricePerDay,
                hostId = route.hostId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.popBackStack(Route.Home, inclusive = false)
                }
            )
        }

        composable<Route.BookingList> {
            BookingListScreen(
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

