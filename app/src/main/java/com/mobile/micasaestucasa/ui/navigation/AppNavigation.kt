package com.mobile.micasaestucasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.ui.screens.auth.LoginScreen
import com.mobile.micasaestucasa.ui.screens.auth.RegisterScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingListMode
import com.mobile.micasaestucasa.ui.screens.booking.BookingListScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingRequestScreen
import com.mobile.micasaestucasa.ui.screens.chat.ChatScreen
import com.mobile.micasaestucasa.ui.screens.chat.ConversationListScreen
import com.mobile.micasaestucasa.ui.screens.home.HomeScreen
import com.mobile.micasaestucasa.ui.screens.profile.ProfileScreen
import com.mobile.micasaestucasa.ui.screens.property.PropertyDetailScree
import com.mobile.micasaestucasa.ui.screens.whishlist.WishlistScreen
import com.mobile.micasaestucasa.ui.viewmodels.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // Reactive auth state
    var currentUserId by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUserId = auth.currentUser?.uid ?: ""
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

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
                    navController.navigate(Route.Trips)
                },
                onNavigateToSaved = { /* TODO */ },
                onNavigateToMessages = {
                    navController.navigate(Route.ConversationList)
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
                },
                onNavigateToHostBookings = {
                    navController.navigate(Route.HostBookings)
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
                onNavigateToChat = { hostId ->
                    navController.navigate(
                        Route.Chat(
                            conversationId = "",
                            hostId = hostId,
                            renterId = currentUserId,
                            propertyId = route.propertyId
                        )
                    )
                }
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

        composable<Route.Trips> {
            BookingListScreen(
                currentUserId = currentUserId,
                mode = BookingListMode.RENTER,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.HostBookings> {
            BookingListScreen(
                currentUserId = currentUserId,
                mode = BookingListMode.HOST,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.ConversationList> {
            ConversationListScreen(
                currentUserId = currentUserId,
                onNavigateToChat = { conversationId ->
                    navController.navigate(
                        Route.Chat(
                            conversationId = conversationId,
                            hostId = "",
                            renterId = "",
                            propertyId = ""
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Chat> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Chat>()
            ChatScreen(
                conversationId = route.conversationId,
                hostId = route.hostId,
                renterId = route.renterId,
                propertyId = route.propertyId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
