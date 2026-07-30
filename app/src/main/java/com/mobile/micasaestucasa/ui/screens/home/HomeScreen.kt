package com.mobile.micasaestucasa.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.ui.components.atomics.ShimmerPropertyCard
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.MiCasaSearchBar
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
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
    onNavigateToMessages: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val currentUser by userViewModel.user.collectAsState()
    val savedIds by homeViewModel.savedPropertyIds.collectAsState()
    var selectedRoute by remember { mutableStateOf("home_screen") }
    var showMap by remember { mutableStateOf(false) }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    androidx.compose.runtime.LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) homeViewModel.loadSavedIds(currentUserId)
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = selectedRoute,
                onItemSelected = { route ->
                    selectedRoute = route
                    when (route) {
                        "profile_screen" -> onNavigateToProfile()
                        "trips_screen" -> onNavigateToTrips()
                        "messages_screen" -> onNavigateToMessages()
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
            // Header con indicatore Admin
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ciao, ${currentUser?.name ?: "Guest"}",
                            fontSize = 14.sp,
                            color = CaptionLabels
                        )
                        Text(
                            text = "Bentornato a casa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingText
                        )
                    }

                    if (currentUser?.roles?.contains(UserRole.ADMIN) == true) {
                        Surface(
                            color = Primario.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = Primario,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Destinazioni",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = HeadingText
                    )
                    androidx.compose.material3.TextButton(onClick = { showMap = !showMap }) {
                        Text(if (showMap) "Vedi Lista" else "Vedi Mappa", color = Primario, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showMap) {
                item {
                    val defaultPosition = LatLng(41.9027835, 12.4963655)
                    val startPosition = homeState.properties.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: defaultPosition
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(startPosition, 5f)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .padding(horizontal = 16.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            homeState.properties.forEach { property ->
                                Marker(
                                    state = MarkerState(position = LatLng(property.latitude, property.longitude)),
                                    title = property.title,
                                    snippet = "€${property.pricePerDay}/notte",
                                    onInfoWindowClick = {
                                        onNavigateToProperty(property.id)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
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
                        isFavorite = savedIds.contains(property.id),
                        onFavoriteClick = { homeViewModel.toggleSaved(currentUserId, property.id) },
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Nessuna proprietà disponibile al momento",
                                    color = CaptionLabels,
                                    fontSize = 15.sp
                                )
                            }
                        }
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
