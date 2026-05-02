package com.mobile.micasaestucasa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.components.atomics.CollectionCategoryItem
import com.mobile.micasaestucasa.ui.components.atomics.ShimmerEffect
import com.mobile.micasaestucasa.ui.components.home.BottomNavigationBar
import com.mobile.micasaestucasa.ui.components.home.Footer
import com.mobile.micasaestucasa.ui.components.home.JournalSection
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.home.SearchBar
import com.mobile.micasaestucasa.ui.components.home.Topnavigation
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography
import com.mobile.micasaestucasa.ui.viewmodels.home.Category
import com.mobile.micasaestucasa.ui.viewmodels.home.HomeUiState
import com.mobile.micasaestucasa.ui.viewmodels.home.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProperty: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = { navController.navigate(Route.Profile) }
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        navController = navController,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchClick = { query -> viewModel.onSearchQueryChanged(query) },
        onNavigateToProperty = onNavigateToProperty,
        onNavigateToProfile = onNavigateToProfile
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    navController: NavController,
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchClick: (String) -> Unit = {},
    onNavigateToProperty: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Topnavigation(
                onProfileClick = onNavigateToProfile
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Header Text
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append("Find your\nhome ")
                            withStyle(style = SpanStyle(color = Primario, fontStyle = FontStyle.Italic)) {
                                append("away")
                            }
                            append("\nfrom home.")
                        },
                        style = Typography.headlineLarge.copy(
                            fontSize = 48.sp,
                            lineHeight = 56.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 2. Search Bar Molecule
            item {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    onSearchClick = onSearchClick
                )
            }

            // 3. Curated Collections
            item {
                Column(modifier = Modifier.padding(vertical = 24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Curated Collections",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { /* View all */ }) {
                            Text("View all", color = Primario, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.categories) { category ->
                            val icon = when (category.icon) {
                                "holiday_village" -> Icons.Default.HolidayVillage
                                "cabin" -> Icons.Default.Cabin
                                "beach_access" -> Icons.Default.BeachAccess
                                "castle" -> Icons.Default.Castle
                                "apartment" -> Icons.Default.Apartment
                                else -> Icons.Default.HolidayVillage
                            }
                            CollectionCategoryItem(
                                icon = icon,
                                label = category.name,
                                isSelected = category.name == "Modern" // Example state
                            )
                        }
                    }
                }
            }

            // 4. Featured Properties Grid / List
            if (uiState.isLoading) {
                items(3) {
                    ShimmerPropertyCard()
                }
            } else if (uiState.error != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                items(uiState.properties) { property ->
                    PropertyCard(
                        name = property.title,
                        rating = property.rating,
                        location = property.city,
                        price = property.pricePerDay,
                        imageUrl = property.imageUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                        isAvailable = true,
                        onClick = { onNavigateToProperty(property.id) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // 5. Journal Section
            item {
                JournalSection(
                    onReadMoreClick = { /* Hoisting example */ }
                )
            }

            // 6. Footer
            item {
                Footer()
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
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
            ShimmerEffect(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f))
            Column(modifier = Modifier.padding(20.dp)) {
                ShimmerEffect(modifier = Modifier.width(200.dp).height(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerEffect(modifier = Modifier.width(150.dp).height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val dummyNavController = rememberNavController()
    MiCasaEsTuCasaTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                properties = listOf(
                    Property(
                        id = "1",
                        ownerId = "owner1",
                        title = "Luxury Villa in Malibu",
                        description = "A beautiful luxury villa with ocean view.",
                        latitude = 34.0259,
                        longitude = -118.7798,
                        city = "Malibu",
                        pricePerDay = 450.0,
                        capacity = 6,
                        keywords = listOf("luxury", "ocean", "villa"),
                        imageUrls = listOf("https://images.unsplash.com/photo-1512917774080-9991f1c4c750"),
                        availableFrom = "2024-01-01",
                        availableTo = "2024-12-31",
                        rating = 4.9,
                        reviewsCount = 24
                    )
                ),
                categories = listOf(
                    Category("Modern", "holiday_village"),
                    Category("Rustic", "cabin"),
                    Category("Beachfront", "beach_access"),
                    Category("Historic", "castle")
                )
            ),
            navController = dummyNavController
        )
    }
}
