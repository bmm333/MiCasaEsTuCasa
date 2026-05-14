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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.ui.components.home.PropertyCard
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.WishlistUiState
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.WishlistViewModel

@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    // Ora il riferimento verrà risolto correttamente grazie all'import
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primario)
        }
    } else {
        WishlistContent(
            uiState = uiState,
            navController = navController,
            onTabSelected = { index -> viewModel.selectTab(index) } // 2. PASSAGGIO LAMBDA
        )
    }
}
@Composable
fun WishlistContent(
    uiState: WishlistUiState,
    navController: NavController,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "saved_screen", // Imposta Saved come selezionato
                onItemSelected = { route ->
                    when (route) {
                        "profile_screen" -> navController.navigate("profile_screen")
                        "trips_screen" -> navController.navigate("trips_screen")
                        "saved_screen" -> navController.navigate("saved_screen")
                        "home_screen" -> navController.navigate("home_screen")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
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
                            // 4. COLLEGAMENTO LOGICA TAB
                            modifier = Modifier.clickable { onTabSelected(index) }
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
