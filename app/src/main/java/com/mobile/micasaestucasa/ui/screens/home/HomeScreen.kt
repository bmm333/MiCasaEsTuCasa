package com.mobile.micasaestucasa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.mobile.micasaestucasa.ui.components.atomics.ShimmerEffect
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.MiCasaTopBar
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.auth.AuthViewModel
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyUiState
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyViewModel
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProperty: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToSaved: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val propertyUiState by propertyViewModel.uiState.collectAsState()
    val currentUser by userViewModel.user.collectAsState()
    var selectedRoute by remember { mutableStateOf("home_screen") }

    LaunchedEffect(Unit) {
        propertyViewModel.searchProperties(
            city = "Vercelli",
            startDate = "",
            endDate = "",
            capacity = 1
        )
    }
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
                        "home_screen" -> { /* gia qui */ }
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

            //proprieta in evidenza
            item {
                Text(
                    text = "In evidenza",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = HeadingText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when (val state = propertyUiState) {
                is PropertyUiState.Loading -> {
                    items(3) {
                        ShimmerPropertyCard()
                    }
                }

                is PropertyUiState.SearchSuccess -> {
                    items(state.properties) { property ->
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
                }

                is PropertyUiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                color = ErrorColor
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
//pill form come gli altri
@Composable
fun MiCasaSearchBar(
    locationText: String,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(CardSurface)
            .clickable { onSearchClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search",
            tint = Primario,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = locationText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = HeadingText
            )
            Text(
                text = "Qualsiasi data · Aggiungi ospiti",
                fontSize = 12.sp,
                color = CaptionLabels
            )
        }
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(ScreenBackground)
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = "Filters",
                tint = HeadingText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ShimmerPropertyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(200.dp)
                        .height(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerEffect(
                    modifier = Modifier
                        .width(150.dp)
                        .height(16.dp)
                )
            }
        }
    }
}
