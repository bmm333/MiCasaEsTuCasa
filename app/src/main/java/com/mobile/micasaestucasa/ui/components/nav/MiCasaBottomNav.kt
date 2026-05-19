package com.mobile.micasaestucasa.ui.components.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.Primario

/**
 * Data class representing a single item.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Default tabs show in the bottom nav bar.
 */
object DefaultBottomNavItems {
    val items = listOf(
        BottomNavItem("home_screen",     "Esplora",  Icons.Rounded.Search),
        BottomNavItem("saved_screen",    "Salvati",  Icons.Rounded.FavoriteBorder),
        BottomNavItem("trips_screen",    "Viaggi",   Icons.Rounded.Luggage),
        BottomNavItem("messages_screen", "Messaggi", Icons.Rounded.ChatBubbleOutline),
        BottomNavItem("profile_screen",  "Profilo",  Icons.Rounded.PersonOutline)
    )
}

@Composable
fun MiCasaBottomNav(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(55.dp))
                .background(CardSurface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.route == selectedRoute

                val tint by animateColorAsState(
                    targetValue = if (isSelected) Primario else CaptionLabels,
                    label = "navTint"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "navScale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Primario.copy(alpha = 0.08f),
                                    RoundedCornerShape(16.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onItemSelected(item.route) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(scale)
                    )
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tint
                    )
                }
            }
        }
    }
}
