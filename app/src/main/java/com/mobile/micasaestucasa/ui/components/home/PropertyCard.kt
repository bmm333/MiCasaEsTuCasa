package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
@Preview(showBackground = true, name = "Test")
fun PropertyCard(
    imageRes: Int = android.R.drawable.ic_menu_gallery,
    name: String = "Villa Test",
    rating: Double = 4.5,
    location: String = "Roma",
    price: Int = 100,
    isAvailable: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.aspectRatio(0.8f)) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )

                if (isAvailable) {
                    Text(
                        text = "AVAILABLE",
                        style = Typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                            .background(Secondary, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { /* Azione preferiti */ },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Aggiungi ai preferiti",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .offset(y = (-32).dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Primario,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rating.toString(),
                            style = Typography.bodyLarge,
                            color = Primario,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = location,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$$price",
                        style = Typography.titleLarge,
                        color = Primario,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / night",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
