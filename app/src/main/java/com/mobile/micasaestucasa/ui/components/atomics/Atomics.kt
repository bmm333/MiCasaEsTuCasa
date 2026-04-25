package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.theme.AppShapes
import com.mobile.micasaestucasa.ui.theme.IconSize
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Spacing
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
fun AppAvatar(
    imageRes: Int,
    size: Dp = IconSize.Avatar,
    showBorder: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, AppShapes.Avatar)
                } else {
                    Modifier
                }
            )
            .padding(if (showBorder) 2.dp else 0.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(AppShapes.Avatar)
        )
    }
}

@Composable
fun RatingBadge(rating: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), AppShapes.Badge)
            .padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Primario,
            modifier = Modifier.size(IconSize.Small)
        )
        Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
        Text(
            text = rating.toString(),
            style = Typography.bodyLarge, // Utilizzo dei font del tema
            color = Primario
        )
    }
}
