package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Euro
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.Caution
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.SecondaryText

@Composable
fun PropertyDetailContent(
    property: Property,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // images gallery
        item {
            PropertyImageGallery(imageUrls = property.imageUrls)
        }

        // title, city , rating
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = property.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = property.city,
                        fontSize = 15.sp,
                        color = CaptionLabels
                    )
                    if (property.rating > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("★", color = Caution, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${String.format("%.1f", property.rating)} (${property.reviewsCount} reviews)",
                            fontSize = 13.sp,
                            color = SecondaryText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            HorizontalDivider(color = BorderDivider, thickness = 1.dp)
        }

        // quick info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InfoChip(
                    icon = Icons.Rounded.People,
                    label = "${property.capacity} guests"
                )
                InfoChip(
                    icon = Icons.Rounded.CalendarMonth,
                    label = "From ${property.availableFrom}"
                )
                InfoChip(
                    icon = Icons.Rounded.Euro,
                    label = "€${property.pricePerDay.toInt()}/night"
                )
            }
            HorizontalDivider(color = BorderDivider, thickness = 1.dp)
        }

        // descp
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = property.description,
                    fontSize = 15.sp,
                    color = SecondaryText,
                    lineHeight = 22.sp
                )
            }
            HorizontalDivider(color = BorderDivider, thickness = 1.dp)
        }

        // keywords
        if (property.keywords.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Charateristics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeadingText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(property.keywords) { keyword ->
                            KeywordChip(keyword)
                        }
                    }
                }
                HorizontalDivider(color = BorderDivider, thickness = 1.dp)
            }
        }

        // position map
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Position",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(8.dp))
                val propertyLatLng = remember(property.latitude, property.longitude) {
                    LatLng(property.latitude, property.longitude)
                }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(propertyLatLng, 13f)
                }
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = rememberMarkerState(position = propertyLatLng),
                        title = property.title,
                        snippet = property.city
                    )
                }
            }
        }
    }
}
