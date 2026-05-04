package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

sealed class BottomNavItem(val route: Any, val icon: ImageVector, val label: String) {
    object Explore : BottomNavItem(Route.Home, Icons.Default.Search, "Explore")

    object Saved : BottomNavItem(Route.Wishlist, Icons.Default.Favorite, "Saved") // Placeholder for now
    object Trips : BottomNavItem("trips", Icons.Default.TravelExplore, "Trips") // Placeholder for now
    object Profile : BottomNavItem(Route.Profile, Icons.Default.Person, "Profile")
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Explore,
        BottomNavItem.Saved,
        BottomNavItem.Trips,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.White,
        modifier = modifier.testTag("bottom_nav_bar")
    ) {
        items.forEach { item ->
            // 2. Controllo speciale per "trips" che è ancora una stringa
            val isSelected = if (item.route is Route) {
                currentDestination?.hierarchy?.any {
                    it.hasRoute(item.route::class)
                } == true
            } else {
                currentDestination?.route == item.route // Fallback per le stringhe
            }

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = Typography.labelSmall) },
                selected = isSelected,
                onClick = {
                    // 3. Controlliamo se la rotta è un oggetto Route
                    if (item.route is Route) {
                        navController.navigate(item.route) {
                            // Salva lo stato e gestisci il back stack[cite: 1]
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.testTag("nav_item_${item.label}"),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primario,
                    selectedTextColor = Primario,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
