package com.mobile.micasaestucasa.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.profile.HostBanner
import com.mobile.micasaestucasa.ui.components.profile.HostDashboardCard
import com.mobile.micasaestucasa.ui.components.profile.PersonalInfoCard
import com.mobile.micasaestucasa.ui.components.profile.ProfileHeader
import com.mobile.micasaestucasa.ui.components.profile.ProfileSectionCard
import com.mobile.micasaestucasa.ui.components.profile.SettingsRow
import com.mobile.micasaestucasa.ui.components.profile.WishlistCard
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.auth.AuthViewModel
import com.mobile.micasaestucasa.ui.viewmodels.user.HostStats
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.WishlistViewModel
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.WishlistUiState

@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
    onNavigateToSettings: (String) -> Unit = {},
    onLogoutNavigate: () -> Unit = {},
    onNavigateToHostBookings: () -> Unit = {},
    onNavigateToMyProperties: () -> Unit = {},
    onNavigateToCreateProperty: () -> Unit = {},
    onNavigateToHostIntro: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateBack: () -> Boolean
) {
    val userState by userViewModel.userState.collectAsStateWithLifecycle()
    val hostStats by userViewModel.hostStats.collectAsStateWithLifecycle()
    val isHost by userViewModel.isHost.collectAsStateWithLifecycle()
    val wishlistState by wishlistViewModel.uiState.collectAsStateWithLifecycle()

    // Refresh user & wishlist data when the screen is shown
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                userViewModel.loadUser()
                wishlistViewModel.loadWishlist()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ProfileContent(
        userState = userState,
        wishlistState = wishlistState,
        navController = navController,
        onLogout = { 
            authViewModel.logout()
            onLogoutNavigate()
        },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHostBookings = onNavigateToHostBookings,
        onNavigateToMyProperties = onNavigateToMyProperties,
        onNavigateToCreateProperty = onNavigateToCreateProperty,
        onNavigateToHostIntro = onNavigateToHostIntro,
        onNavigateToAdmin = onNavigateToAdmin,
        onNavigateToEditProfile = onNavigateToEditProfile,
        hostStats = hostStats,
        isHost = isHost
    )
}

@Composable
fun ProfileContent(
    userState: Resource<User?>,
    wishlistState: WishlistUiState,
    hostStats: HostStats,
    isHost: Boolean,
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToSettings: (String) -> Unit,
    onNavigateToHostBookings: () -> Unit = {},
    onNavigateToMyProperties: () -> Unit = {},
    onNavigateToCreateProperty: () -> Unit = {},
    onNavigateToHostIntro: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "profile_screen",
                onItemSelected = { route ->
                    when (route) {
                        "home_screen"     -> navController.navigate(com.mobile.micasaestucasa.ui.navigation.Route.Home) {
                            popUpTo(0)
                        }
                        "saved_screen"    -> navController.navigate(com.mobile.micasaestucasa.ui.navigation.Route.Wishlist)
                        "trips_screen"    -> navController.navigate(com.mobile.micasaestucasa.ui.navigation.Route.Trips)
                        "messages_screen" -> navController.navigate(com.mobile.micasaestucasa.ui.navigation.Route.ConversationList)
                        "profile_screen"  -> { /* already here */ }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (userState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = userState.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry Login")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val user = userState.data
                val memberSince = remember(user?.createdAt) {
                    user?.createdAt?.let {
                        try {
                            java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                        } catch (e: Exception) {
                            "2024"
                        }
                    } ?: "2024"
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProfileHeader(
                            name = listOfNotNull(user?.name, user?.lastName?.takeIf { it.isNotBlank() })
                                .joinToString(" ")
                                .ifBlank { "Guest" },
                            memberSince = memberSince,
                            bio = user?.bio?.takeIf { it.isNotBlank() } ?: "Nessuna biografia inserita",
                            imageUrl = user?.profileImageUrl,
                            badge = user?.badge,
                            onEditClick = { onNavigateToEditProfile() }
                        )
                    }

                    item {
                        PersonalInfoCard(
                            fullName = listOfNotNull(user?.name, user?.lastName?.takeIf { it.isNotBlank() })
                                .joinToString(" ")
                                .ifBlank { "" },
                            email = user?.email ?: "",
                            phone = user?.phone?.takeIf { it.isNotBlank() } ?: "Nessun numero di telefono inserito",
                            address = user?.address?.takeIf { it.isNotBlank() } ?: "Nessun indirizzo inserito",
                            onEditClick = { onNavigateToEditProfile() }
                        )
                    }

                    if (!isHost && user?.roles?.contains(UserRole.ADMIN) == false) {
                        item {
                            PaddingWrapper {
                                HostBanner(onGetStarted = onNavigateToHostIntro)
                            }
                        }
                    }

                    item {
                        val wishlistCount = wishlistState.properties.size
                        val imageUrls = wishlistState.properties.mapNotNull { it.imageUrls.firstOrNull() }
                        WishlistCard(
                            count = wishlistCount,
                            imageUrls = imageUrls,
                            onClick = { navController.navigate(com.mobile.micasaestucasa.ui.navigation.Route.Wishlist) }
                        )
                    }

                    // Admin section
                    if (user?.roles?.contains(UserRole.ADMIN) == true) {
                        item {
                            ProfileSectionCard(
                                title = "Administration",
                                icon = Icons.Default.AdminPanelSettings
                            ) {
                                Column {
                                    SettingsRow(
                                        icon = Icons.Default.Settings,
                                        label = "Admin Panel",
                                        onClick = { onNavigateToAdmin() }
                                    )
                                }
                            }
                        }
                    }

                    // Host section: link to received bookings
                    if (isHost) {
                        item {
                            PaddingWrapper {
                                HostDashboardCard(
                                    stats = hostStats,
                                    onNavigateToMyProperties = onNavigateToMyProperties,
                                    onNavigateToHostBookings = onNavigateToHostBookings,
                                    onNavigateToCreateProperty = onNavigateToCreateProperty
                                )
                            }
                        }
                    }

                    item {
                        ProfileSectionCard(
                            title = "Settings",
                            icon = Icons.Default.Settings
                        ) {
                            Column {
                                SettingsRow(
                                    icon = Icons.Default.Edit,
                                    label = "Modifica profilo",
                                    onClick = { onNavigateToEditProfile() }
                                )
                                SettingsRow(
                                    icon = Icons.Default.Notifications,
                                    label = "Notifications",
                                    onClick = { onNavigateToSettings("notifications") }
                                )
                                SettingsRow(
                                    icon = Icons.Default.PrivacyTip,
                                    label = "Privacy",
                                    onClick = { onNavigateToSettings("privacy") }
                                )
                            }
                        }
                    }

                    item {
                        PaddingWrapper {
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaddingWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val dummyNavController = rememberNavController()
    MiCasaEsTuCasaTheme {
        ProfileContent(
            userState = Resource.Success(
                User(
                    id = "1",
                    name = "Mario Rossi",
                    email = "mario.rossi@example.com",
                    roles = listOf(UserRole.GUEST),
                    bio = "Amo viaggiare e scoprire posti nuovi!",
                    profileImageUrl = null,
                    address = "Via Roma 123, Milano",
                    phone = "+39 333 1234567"
                )
            ),
            wishlistState = WishlistUiState(),
            hostStats = HostStats(),
            isHost = false,
            navController = dummyNavController,
            onLogout = {},
            onNavigateToSettings = {},
            onNavigateToEditProfile = {}
        )
    }
}
