package com.mobile.micasaestucasa.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.ui.components.atomics.ShimmerPropertyCard
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.MiCasaSearchBar
import com.mobile.micasaestucasa.ui.components.nav.MiCasaTopBar
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.home.HomeViewModel
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProperty: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToSaved: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val currentUser by userViewModel.user.collectAsState()
    var selectedRoute by remember { mutableStateOf("home_screen") }
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            MiCasaTopBar(
                userName = currentUser?.name ?: "",
                onAvatarClick = onNavigateToProfile,
                onNotificationsClick = {}
            )
        },
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = selectedRoute,
                onItemSelected = { route ->
                    selectedRoute = route
                    when (route) {
                        "profile_screen" -> onNavigateToProfile()
                        "trips_screen" -> onNavigateToTrips()
                        "saved_screen" -> onNavigateToSaved()
                        "home_screen" -> { /* già qui */ }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // search bar
            item {
                MiCasaSearchBar(
                    locationText = "Dove vuoi andare?",
                    onSearchClick = onNavigateToSearch,
                    onFilterClick = onNavigateToSearch
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // destinazioni disponibili
            item {
                Text(
                    text = "Destinazioni disponibili",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = HeadingText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // loading shimmer
            if (homeState.isLoading && homeState.properties.isEmpty()) {
                items(4) {
                    ShimmerPropertyCard()
                }
            }

            // property cards
            items(homeState.properties) { property ->
                PropertyCard(
                    name = property.title,
                    rating = property.rating,
                    location = property.city,
                    price = property.pricePerDay,
                    imageUrl = property.imageUrls.firstOrNull() ?: "",
                    isAvailable = true,
                    onClick = { onNavigateToProperty(property.id) }
                )
            }

            // empty state
            if (!homeState.isLoading && homeState.properties.isEmpty() && homeState.error == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessuna proprietà disponibile al momento",
                            color = CaptionLabels,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // error state
            if (homeState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = homeState.error ?: "",
                            color = ErrorColor
                        )
                    }
                }
            }
        }
    }
}
