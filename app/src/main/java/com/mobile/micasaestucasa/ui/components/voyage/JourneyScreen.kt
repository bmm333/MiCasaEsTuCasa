package com.mobile.micasaestucasa.ui.components.voyage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobile.micasaestucasa.ui.components.home.BottomNavigationBar

@Composable
fun JourneysScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            item {
                TripsHeader(
                    title = "Your Journeys",
                    subtitle = "Curated memories and upcoming escapes, all in one warm place."
                )
            }

            item { UpcomingHeader(count = 2) }

            item {
                FeaturedTripCard(
                    title = "The Cedar Sanctum",
                    date = "Dec 12 – Dec 18, 2024",
                    location = "Lake Tahoe, California",
                    host = "Elena Vance",
                    imageRes = android.R.drawable.ic_menu_gallery
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                TripsHeader(title = "Past Memories", subtitle = "")
            }

            item {
                PastMemoryItem(
                    title = "The Artist's Loft",
                    date = "October 2023 • Paris, France",
                    rating = 5,
                    imageRes = android.R.drawable.ic_menu_gallery
                )
            }

            item {
                PastMemoryItem(
                    title = "Ocean Driftwood",
                    date = "July 2023 • Malibu, CA",
                    rating = 5,
                    imageRes = android.R.drawable.ic_menu_gallery
                )
            }

            item { NextStepPlaceholder() }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true, name = "Test")
@Composable
fun JourneysScreenPreview() {
    val navController = rememberNavController()
    JourneysScreen(navController = navController)
}
