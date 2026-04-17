package com.mobile.micasaestucasa.ui.components.voyage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.Typography

// 1. Intestazione della pagina
@Composable
fun TripsHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text(
            text = title, 
            style = Typography.headlineLarge, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle, 
            style = Typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 2. Badge "Upcoming" con conteggio
@Composable
fun UpcomingHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Upcoming", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer, 
            shape = CircleShape
        ) {
            Text(
                text = "$count BOOKINGS",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// 3. Card Viaggio Principale
@Composable
fun FeaturedTripCard(title: String, date: String, location: String, host: String, imageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(240.dp)) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(16.dp),
                    color = Secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Confirmed", 
                        color = Color.White, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), 
                        style = Typography.labelSmall
                    )
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = Typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VerifiedUser, 
                        contentDescription = null, 
                        tint = Primario, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = date, 
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                TripInfoRow(Icons.Default.Map, location)
                TripInfoRow(Icons.Default.Person, "Hosted by $host")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Details", color = Primario, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Text("Check-in Guide", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TripInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(
            shape = CircleShape, 
            color = MaterialTheme.colorScheme.surfaceVariant, 
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.padding(8.dp), 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text, 
            style = Typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 14.sp
        )
    }
}

// 4. Elemento Ricordi Passati
@Composable
fun PastMemoryItem(title: String, date: String, rating: Int, imageRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                text = date, 
                style = Typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.padding(top = 4.dp)) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star, 
                        contentDescription = null, 
                        tint = Primario, 
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// 5. Placeholder tratteggiato
@Composable
fun NextStepPlaceholder() {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier.fillMaxWidth().padding(24.dp).height(140.dp).drawBehind {
            drawRoundRect(
                color = outlineVariant.copy(alpha = 0.5f),
                style = Stroke(
                    width = 2f, 
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                ),
                cornerRadius = CornerRadius(24.dp.toPx())
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AutoAwesome, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.outline, 
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Where will you go next?", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "EXPLORE HOMES", 
                color = Primario, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 12.dp), 
                style = Typography.labelSmall
            )
        }
    }
}
