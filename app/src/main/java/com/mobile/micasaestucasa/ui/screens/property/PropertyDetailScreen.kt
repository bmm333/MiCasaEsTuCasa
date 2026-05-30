package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.components.property.AmenityItem
import com.mobile.micasaestucasa.ui.components.property.BookingBottomBar
import com.mobile.micasaestucasa.ui.components.property.FeatureChip
import com.mobile.micasaestucasa.ui.components.property.TransportCard
import com.mobile.micasaestucasa.ui.theme.Accenti
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Typography
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyUiState
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyViewModel

// ── Keyword → Icon mapping ──────────────────────────────────────────────
private val keywordIconMap: Map<String, ImageVector> = mapOf(
    "wifi" to Icons.Default.Wifi,
    "pool" to Icons.Default.Pool,
    "parking" to Icons.Default.LocalParking,
    "kitchen" to Icons.Default.Kitchen,
    "laundry" to Icons.Default.LocalLaundryService,
    "workspace" to Icons.Default.Laptop,
    "bathtub" to Icons.Default.Bathtub,
)

private fun iconForKeyword(keyword: String): ImageVector {
    val lower = keyword.lowercase()
    return keywordIconMap.entries
        .firstOrNull { lower.contains(it.key) }
        ?.value ?: Icons.Default.Star
}

// ═══════════════════════════════════════════════════════════════════════
//  Property Detail Screen  – entry point with ViewModel wiring
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun PropertyDetailScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onNavigateToChat: (String) -> Unit = {},
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(propertyId) {
        viewModel.loadPropertyDetail(propertyId)
    }

    when (val state = uiState) {
        is PropertyUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primario)
            }
        }

        is PropertyUiState.DetailSuccess -> {
            PropertyDetailContent(
                property = state.property,
                onNavigateBack = onNavigateBack,
                onBookClick = {
                    onNavigateToBooking(
                        propertyId,
                        state.property.title,
                        state.property.pricePerDay.toString(),
                        state.property.ownerId
                    )
                },
                onChatClick = { onNavigateToChat(state.property.ownerId) }
            )
        }

        is PropertyUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        color = ErrorColor,
                        style = Typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "← Torna indietro",
                        color = Primario,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onNavigateBack() }
                    )
                }
            }
        }

        else -> { /* Idle – do nothing */ }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Content  – pure UI, receives Property directly (preview-friendly)
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun PropertyDetailContent(
    property: Property,
    onNavigateBack: () -> Unit = {},
    onBookClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BookingBottomBar(
                price = property.pricePerDay.toInt().toString(),
                dates = "${property.availableFrom} — ${property.availableTo}",
                onBookClick = onBookClick,
                onChatClick = onChatClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // ── 1. Image Carousel ──────────────────────────────────────
            item {
                ImageCarousel(
                    imageUrls = property.imageUrls,
                    onBackClick = onNavigateBack
                )
            }

            // ── 2. Title + Location + Rating ─────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = property.title,
                        style = Typography.headlineLarge.copy(fontSize = 28.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Primario,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = property.city,
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Accenti,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (property.rating > 0) "%.1f".format(property.rating) else "New",
                                style = Typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (property.reviewsCount > 0) {
                                Text(
                                    text = " (${property.reviewsCount})",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. Feature Chips Row ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureChip(
                        icon = Icons.Default.KingBed,
                        label = "Sleeping",
                        value = "${property.capacity} Beds"
                    )
                    FeatureChip(
                        icon = Icons.Default.Group,
                        label = "Guests",
                        value = "${property.capacity} Max"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── 4. Divider ─────────────────────────────────────────────
            item {
                SectionDivider()
            }

            // ── 5. About this home ─────────────────────────────────────
            item {
                AboutSection(description = property.description)
            }

            // ── 6. Divider ─────────────────────────────────────────────
            item {
                SectionDivider()
            }

            // ── 7. What this home offers ───────────────────────────────
            item {
                AmenitiesSection(keywords = property.keywords)
            }

            // ── 8. Divider ─────────────────────────────────────────────
            item {
                SectionDivider()
            }

            // ── 9. Location Section ────────────────────────────────────
            item {
                LocationSection(
                    city = property.city,
                    latitude = property.latitude,
                    longitude = property.longitude
                )
            }

            // ── 10. Getting around ─────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    TransportCard()
                }
            }

            // ── 11. Bottom spacing ─────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Sub-components
// ═══════════════════════════════════════════════════════════════════════

/** Fullscreen-width image pager with back / share / fav overlay */
@Composable
private fun ImageCarousel(
    imageUrls: List<String>,
    onBackClick: () -> Unit
) {
    val images = imageUrls.ifEmpty {
        listOf("https://images.unsplash.com/photo-1600585154340-be6161a56a0c")
    }
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxWidth()) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = "Property image ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient overlay top (for readability of back button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna indietro",
                    tint = Color.White
                )
            }

            Row {
                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Condividi",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { /* Favorite */ },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Aggiungi ai preferiti",
                        tint = Color.White
                    )
                }
            }
        }

        // Page indicator
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.35f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                images.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

/** Section divider */
@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

/** About / description section with expandable text */
@Composable
private fun AboutSection(description: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .animateContentSize()
    ) {
        Text(
            text = "About this home",
            style = Typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = description.ifEmpty { "No description provided." },
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        if (description.length > 150) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (expanded) "Show less" else "Read more about the experience →",
                style = Typography.bodyMedium,
                color = Primario,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

/** Amenities grid from keywords */
@Composable
private fun AmenitiesSection(keywords: List<String>) {
    val displayedKeywords = if (keywords.isEmpty()) {
        listOf("WiFi", "Pool", "Parking", "Kitchen")
    } else {
        keywords
    }
    var showAll by remember { mutableStateOf(false) }
    val visibleItems = if (showAll) displayedKeywords else displayedKeywords.take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "What this home offers",
            style = Typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        visibleItems.forEach { keyword ->
            AmenityItem(
                icon = iconForKeyword(keyword),
                label = keyword.replaceFirstChar { it.uppercase() }
            )
        }

        if (displayedKeywords.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showAll) "Show less" else "Show all ${displayedKeywords.size} amenities",
                style = Typography.bodyMedium,
                color = Primario,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { showAll = !showAll }
            )
        }
    }
}

/** Location block with city name and coordinates */
@Composable
private fun LocationSection(
    city: String,
    latitude: Double,
    longitude: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Location",
            style = Typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Location pill
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Primario,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = city.ifEmpty { "Location not specified" },
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (latitude != 0.0 && longitude != 0.0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%.4f, %.4f".format(latitude, longitude),
                style = Typography.labelSmall,
                color = CaptionLabels
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Preview
// ═══════════════════════════════════════════════════════════════════════
@Preview(showBackground = true, showSystemUi = true, name = "Property Detail")
@Composable
fun PropertyDetailScreenPreview() {
    val fakeProperty = Property(
        id = "preview_1",
        ownerId = "owner_123",
        title = "The Earth & Clay Villa",
        description = "Perched on the edges of the ancient caldera, The Earth & Clay Villa is one " +
                "better to be believed with one's eyes. Every corner has been curated with raw, " +
                "minimal materials — dark travertine stone, hand-woven linens and local pottery — " +
                "to create an atmosphere of profound tranquility.\n\n" +
                "Wake up to the scent of wild sage and sea salt. This expansive terrace offers " +
                "the kind of ocean view of the Aegean, where the blue of the sea melts into the " +
                "sky in an endless horizon. This isn't just a place to stay; it is an invitation to breathe.",
        latitude = 36.4161,
        longitude = 25.4322,
        city = "Santorini, Greece",
        pricePerDay = 450.0,
        capacity = 6,
        keywords = listOf(
            "Private Infinity Pool",
            "High-speed Fiber WiFi",
            "Chef's Kitchen",
            "Free Valet Parking",
            "Dedicated Workspace",
            "In-suite Laundry"
        ),
        imageUrls = listOf(
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
            "https://images.unsplash.com/photo-1613490493576-7fde63acd811",
            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750"
        ),
        availableFrom = "2026-06-12",
        availableTo = "2026-06-18",
        rating = 4.95,
        reviewsCount = 128
    )

    MiCasaEsTuCasaTheme {
        PropertyDetailContent(
            property = fakeProperty,
            onNavigateBack = {}
        )
    }
}
