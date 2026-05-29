package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.components.atomics.MiCasaPrimaryButton
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.SkeletonLoader

@Composable
fun PropertyBottomBar(
    property: Property,
    onBook: () -> Unit,
    onChat: () -> Unit
) {
    Surface(
        color = CardSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "€${property.pricePerDay.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = HeadingText
                )
                Text(
                    text = "per night",
                    fontSize = 12.sp,
                    color = CaptionLabels
                )
            }

            // chat
            IconButton(
                onClick = onChat,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SkeletonLoader)
            ) {
                Icon(
                    Icons.Rounded.Chat,
                    contentDescription = "Chat con host",
                    tint = HeadingText,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Cazzo e porca troia perche non abbiamo il primary button definito
            MiCasaPrimaryButton(
                text = "Book",
                onClick = onBook,
                modifier = Modifier.width(130.dp)
            )
        }
    }
}
