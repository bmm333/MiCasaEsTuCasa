package com.mobile.micasaestucasa.ui.screens.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.components.home.BottomNavigationBar
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.home.Topnavigation
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.WishlistUiState

@Composable
fun WishlistScreen(
    navController: NavController,
    // viewModel: WishlistViewModel = hiltViewModel() // Integrerai qui il ViewModel
) {
    // Simulazione stato per la demo, da sostituire con: val uiState by viewModel.uiState.collectAsState()
    val uiState = WishlistUiState(
        collections = listOf(
            Collection("1", "Summer 2025", 5, "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2"),
            Collection("2", "Dream Homes", 4, "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d")
        ),
        properties = listOf(
            Property(
                id = "1",
                ownerId = "o1",
                title = "Luxury Villa Oasis",
                description = "A beautiful luxury villa in Ibiza.",
                latitude = 38.9067,
                longitude = 1.4206,
                city = "Ibiza",
                pricePerDay = 850.0,
                capacity = 6,
                keywords = listOf("luxury", "pool", "beach"),
                imageUrls = listOf("https://images.unsplash.com/photo-1600596542815-ffad4c1539a9"),
                availableFrom = "2025-01-01",
                availableTo = "2025-12-31",
                rating = 4.92
            ),
            Property(
                id = "2",
                ownerId = "o2",
                title = "Cozy Pine Cabin",
                description = "A cozy cabin in the woods of Aspen.",
                latitude = 39.1911,
                longitude = -106.8175,
                city = "Aspen",
                pricePerDay = 420.0,
                capacity = 4,
                keywords = listOf("cabin", "snow", "cozy"),
                imageUrls = listOf("https://images.unsplash.com/photo-1510798831971-661eb04b3739"),
                availableFrom = "2025-01-01",
                availableTo = "2025-12-31",
                rating = 4.85
            )
        )
    )

    WishlistContent(uiState, navController)
}

@Composable
fun WishlistContent(
    uiState: WishlistUiState,
    navController: NavController
) {
    Scaffold(
        topBar = { Topnavigation(onProfileClick = { /* Nav to profile */ }) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA)), // Sfondo Surface base dal DS
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. HEADER & "THE SUN BUTTON" (Yellow CTA)
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wishlist",
                                style = Typography.headlineLarge.copy(fontSize = 44.sp),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${uiState.properties.size} ITEMS SAVED",
                                style = Typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                        // Primary CTA "The Sun Button" [Source 2]
                        Button(
                            onClick = { /* Action */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFE54)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("CREATE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. EDITORIAL TABS (No border lines, only spacing) [Source 2]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    val tabs = listOf("All Saves", "Collections", "Shared")
                    tabs.forEachIndexed { index, tab ->
                        Text(
                            text = tab,
                            style = Typography.titleMedium,
                            color = if (uiState.selectedTab == index) Primario else Color.Gray,
                            textDecoration = if (uiState.selectedTab == index) TextDecoration.Underline else null,
                            modifier = Modifier.clickable { /* Select Tab */ }
                        )
                    }
                }
            }

            // 3. COLLECTIONS SECTION (Horizontal Layering)
            item {
                Column(modifier = Modifier.padding(top = 40.dp)) {
                    Text(
                        text = "Your Collections",
                        style = Typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Add New Collection Card (Architectural Chip style)
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFE7E8E8)), // surface-container
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                }
                                Text("New", style = Typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        // Existing Collections
                        items(uiState.collections) { collection ->
                            WishlistCollectionCard(collection)
                        }
                    }
                }
            }

            // 4. PROPERTY GRID (Vertical White Space instead of lines) [Source 2]
            item { Spacer(Modifier.height(40.dp)) }

            items(uiState.properties) { property ->
                PropertyCard(
                    name = property.title,
                    rating = property.rating,
                    location = property.city,
                    price = property.pricePerDay,
                    imageUrl = property.imageUrls.firstOrNull() ?: "",
                    isAvailable = true,
                    onClick = { /* Nav to detail */ }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WishlistCollectionCard(collection: Collection) {
    Column(
        modifier = Modifier.width(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = collection.coverImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = collection.name,
            style = Typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "${collection.propertyCount} properties",
            style = Typography.labelSmall,
            color = Color.Gray
        )
    }
}

// PREVIEW DEFINITIVA
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun WishlistScreenPreview() {
    val navController = rememberNavController()
    MiCasaEsTuCasaTheme {
        WishlistScreen(navController)
    }
}
