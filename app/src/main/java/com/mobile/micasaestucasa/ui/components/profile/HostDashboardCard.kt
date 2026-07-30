package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Badges
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Sfumatura
import com.mobile.micasaestucasa.ui.viewmodels.user.HostStats

@Composable
fun HostDashboardCard(
    stats: HostStats,
    onNavigateToMyProperties: () -> Unit,
    onNavigateToHostBookings: () -> Unit,
    onNavigateToCreateProperty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Sfumatura),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, tint = Primario)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Gestisci le tue proprietà",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HeadingText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        if (stats.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)) // Simple shimmer placeholder
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    value = stats.propertyCount.toString(),
                    label = "Proprietà",
                    color = HeadingText
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    value = stats.bookingCount.toString(),
                    label = "Prenotazioni",
                    color = HeadingText
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    value = "€${stats.totalRevenue.toInt()}",
                    label = "Guadagni",
                    color = Badges
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SettingsRow(
                icon = Icons.Default.Home,
                label = "Le mie proprietà",
                onClick = onNavigateToMyProperties
            )
            SettingsRow(
                icon = Icons.Default.CalendarMonth,
                label = "Richieste di prenotazione",
                onClick = onNavigateToHostBookings
            )
            SettingsRow(
                icon = Icons.Default.AddBusiness,
                label = "Aggiungi nuova proprietà",
                onClick = onNavigateToCreateProperty
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Sfumatura.copy(alpha = 0.3f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = CaptionLabels,
            fontWeight = FontWeight.Medium
        )
    }
}
