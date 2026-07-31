package com.mobile.micasaestucasa.ui.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.ui.screens.admin.AdminScreen
import com.mobile.micasaestucasa.ui.screens.auth.LoginScreen
import com.mobile.micasaestucasa.ui.screens.auth.PostSignupChoiceScreen
import com.mobile.micasaestucasa.ui.screens.auth.RegisterScreen
import com.mobile.micasaestucasa.ui.screens.auth.SignupOnboardingScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingListMode
import com.mobile.micasaestucasa.ui.screens.booking.BookingListScreen
import com.mobile.micasaestucasa.ui.screens.booking.TripsScreen
import com.mobile.micasaestucasa.ui.screens.booking.BookingRequestScreen
import com.mobile.micasaestucasa.ui.screens.chat.ChatScreen
import com.mobile.micasaestucasa.ui.screens.chat.ConversationListScreen
import com.mobile.micasaestucasa.ui.screens.home.HomeScreen
import com.mobile.micasaestucasa.ui.screens.host.CreatePropertyScreen
import com.mobile.micasaestucasa.ui.screens.host.HostIntroScreen
import com.mobile.micasaestucasa.ui.screens.host.MyPropertiesScreen
import com.mobile.micasaestucasa.ui.screens.profile.EditProfileScreen
import com.mobile.micasaestucasa.ui.screens.profile.ProfileScreen
import com.mobile.micasaestucasa.ui.screens.property.PropertyDetailScreen
import com.mobile.micasaestucasa.ui.screens.search.SearchScreen
import com.mobile.micasaestucasa.ui.screens.whishlist.WishlistScreen
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.viewmodels.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel = hiltViewModel(),
    pendingNotificationType: String? = null,
    pendingNotificationTargetId: String? = null,
    onNotificationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val sessionEvent by viewModel.sessionEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentUserId by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUserId = auth.currentUser?.uid ?: ""
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primario)
        }
        return
    }

    LaunchedEffect(sessionEvent) {
        when (sessionEvent) {
            MainViewModel.SessionEvent.Banned,
            MainViewModel.SessionEvent.Suspended -> {
                navController.navigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(startDestination, pendingNotificationType, pendingNotificationTargetId) {
        if (pendingNotificationType.isNullOrBlank()) return@LaunchedEffect
        when (pendingNotificationType) {
            "NEW_BOOKING_REQUEST" -> navController.navigate(Route.HostBookings)
            "BOOKING_ACCEPTED", "BOOKING_REJECTED", "BOOKING_CANCELLED" -> navController.navigate(Route.Trips)
            "NEW_MESSAGE" -> {
                val convId = pendingNotificationTargetId.orEmpty()
                if (convId.isNotBlank()) {
                    navController.navigate(
                        Route.Chat(
                            conversationId = convId,
                            hostId = "",
                            renterId = "",
                            propertyId = ""
                        )
                    )
                } else {
                    navController.navigate(Route.ConversationList)
                }
            }
        }
        onNotificationHandled()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
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
                    navController.navigate(Route.SignupOnboarding) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.SignupOnboarding> {
            SignupOnboardingScreen(
                onCompleted = {
                    navController.navigate(Route.PostSignupChoice) {
                        popUpTo(Route.SignupOnboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.PostSignupChoice> {
            PostSignupChoiceScreen(
                onChooseRent = {
                    navController.navigate(Route.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChooseHost = {
                    navController.navigate(Route.HostIntro)
                }
            )
        }

        composable<Route.HostIntro> {
            HostIntroScreen(
                onNavigateBack = {
                    navController.navigate(Route.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGetStarted = {
                    navController.navigate(Route.CreateProperty)
                },
                onSkip = {
                    navController.navigate(Route.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.CreateProperty> {
            CreatePropertyScreen(
                ownerId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onPublished = {
                    navController.navigate(Route.MyProperties) {
                        popUpTo(Route.Home) { inclusive = false }
                    }
                }
            )
        }

        composable<Route.MyProperties> {
            MyPropertiesScreen(
                ownerId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                },
                onNavigateToCreateProperty = {
                    navController.navigate(Route.CreateProperty)
                },
                onNavigateToEditProperty = { propertyId ->
                    navController.navigate(Route.EditProperty(propertyId))
                },
                navController = navController
            )
        }

        composable<Route.EditProperty> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.EditProperty>()
            CreatePropertyScreen(
                ownerId = currentUserId,
                propertyId = route.propertyId,
                onNavigateBack = { navController.popBackStack() },
                onPublished = { navController.popBackStack() }
            )
        }

        composable<Route.Home> {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSearch = { navController.navigate(Route.Search) },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                },
                onNavigateToTrips = {
                    navController.navigate(Route.Trips)
                },
                onNavigateToSaved = {
                    navController.navigate(Route.Wishlist)
                },
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
                },
                onNavigateToMyProperties = {
                    navController.navigate(Route.MyProperties)
                },
                onNavigateToCreateProperty = {
                    navController.navigate(Route.CreateProperty)
                },
                onNavigateToHostIntro = {
                    navController.navigate(Route.HostIntro)
                },
                onNavigateToAdmin = {
                    navController.navigate(Route.Admin)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Route.EditProfile)
                },
                onNavigateToSettings = { settingType ->
                    when (settingType) {
                        "notifications" -> {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                        "privacy" -> {
                            android.widget.Toast.makeText(
                                context,
                                "Impostazioni privacy in arrivo",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            android.widget.Toast.makeText(
                                context,
                                "Funzionalità in arrivo",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        composable<Route.Wishlist> {
            WishlistScreen(
                navController = navController,
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                }
            )
        }

        composable<Route.PropertyDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PropertyDetail>()
            PropertyDetailScreen(
                propertyId = route.propertyId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBooking = { propId, title, price, hostId, availableFrom, availableTo ->
                    navController.navigate(
                        Route.BookingRequest(
                            propertyId = propId,
                            propertyTitle = title,
                            pricePerDay = price.toDoubleOrNull() ?: 0.0,
                            hostId = hostId,
                            availableFrom = availableFrom,
                            availableTo = availableTo
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
                availableFrom = route.availableFrom,
                availableTo = route.availableTo,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.popBackStack(Route.Home, inclusive = false)
                }
            )
        }

        composable<Route.Trips> {
            TripsScreen(
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                },
                onNavigateToChat = { hostId, renterId, propertyId ->
                    navController.navigate(
                        Route.Chat(
                            conversationId = "",
                            hostId = hostId,
                            renterId = renterId,
                            propertyId = propertyId
                        )
                    )
                },
                navController = navController
            )
        }

        composable<Route.HostBookings> {
            BookingListScreen(
                currentUserId = currentUserId,
                mode = BookingListMode.HOST,
                onNavigateBack = { navController.popBackStack() },
                navController = navController,
                onNavigateToChat = { hostId, renterId, propertyId ->
                    navController.navigate(
                        Route.Chat(
                            conversationId = "",
                            hostId = hostId,
                            renterId = renterId,
                            propertyId = propertyId
                        )
                    )
                }
            )
        }

        composable<Route.ConversationList> {
            ConversationListScreen(
                currentUserId = currentUserId,
                onNavigateToChat = { conversation ->
                    navController.navigate(
                        Route.Chat(
                            conversationId = conversation.id,
                            hostId = conversation.hostId,
                            renterId = conversation.renterId,
                            propertyId = conversation.propertyId
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() },
                navController = navController
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProperty = { propId ->
                    navController.navigate(Route.PropertyDetail(propId))
                }
            )
        }

        composable<Route.EditProfile> {
            EditProfileScreen(
                onProfileSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Search> {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                }
            )
        }

        composable<Route.Admin> {
            AdminScreen(
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProperty = { propertyId ->
                    navController.navigate(Route.PropertyDetail(propertyId))
                }
            )
        }
    }
}
