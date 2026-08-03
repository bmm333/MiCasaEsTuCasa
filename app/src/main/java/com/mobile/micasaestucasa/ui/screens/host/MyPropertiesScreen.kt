package com.mobile.micasaestucasa.ui.screens.host

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.components.nav.HostBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.MiCasaConnectedBottomNav
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyUiState
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyViewModel

@Composable
fun MyPropertiesScreen(
    ownerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProperty: (String) -> Unit,
    onNavigateToCreateProperty: () -> Unit,
    onNavigateToEditProperty: (String) -> Unit,
    navController: NavController,
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ownerId) {
        viewModel.loadOwnerProperties(ownerId)
    }

    Scaffold(
        bottomBar = {
            MiCasaConnectedBottomNav(
                items = HostBottomNavItems.items,
                selectedRoute = "host_properties",
                onItemSelected = { route ->
                    when (route) {
                        "host_properties" -> {} // Already here
                        "host_bookings" -> navController.navigate(Route.HostBookings) {
                            popUpTo(Route.Profile)
                        }
                        "exit_host" -> onNavigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color(0xFF222222))
                }
                Text(
                    "Le mie proprietà",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNavigateToCreateProperty) {
                    Icon(Icons.Rounded.Add, null, tint = Primario)
                }
            }

            when (val state = uiState) {
                is PropertyUiState.Loading, is PropertyUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Primario)
                    }
                }

                is PropertyUiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(state.message, color = CaptionLabels, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
                    }
                }

                is PropertyUiState.OwnerSuccess -> {
                    val properties = state.properties
                    if (properties.isEmpty()) {
                        EmptyHostState(onNavigateToCreateProperty)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "${properties.size} annunci pubblicati",
                                    fontSize = 14.sp,
                                    color = CaptionLabels,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            items(properties, key = { it.id }) { property ->
                                HostPropertyCard(
                                    property = property,
                                    onClick = { onNavigateToProperty(property.id) },
                                    onEdit = { onNavigateToEditProperty(property.id) },
                                    onDelete = { viewModel.deleteProperty(property.id, ownerId) }
                                )
                            }
                        }
                    }
                }

                else -> {
                    // SearchSuccess or DetailSuccess — shouldn't happen here, just show loader
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Primario)
                    }
                }
            }
        }
    }
}

@Composable
private fun HostPropertyCard(
    property: Property,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina proprietà", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sei sicuro di voler eliminare \"${property.title}\"? Questa azione non è reversibile.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE5474B))
                ) {
                    Text("Elimina", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(BorderDivider)
        ) {
            if (property.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = property.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Rounded.Home, null, tint = CaptionLabels, modifier = Modifier.align(Alignment.Center).size(32.dp))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(property.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF222222), maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(property.city, fontSize = 13.sp, color = CaptionLabels)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${property.rating}", fontSize = 12.sp, color = CaptionLabels)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.People, null, tint = CaptionLabels, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${property.capacity}", fontSize = 12.sp, color = CaptionLabels)
                }
                Text("€${property.pricePerDay.toInt()}/notte", fontSize = 12.sp, color = Primario, fontWeight = FontWeight.SemiBold)
            }
        }
        // Edit button
        IconButton(onClick = onEdit) {
            Icon(Icons.Rounded.Edit, contentDescription = "Modifica", tint = Primario)
        }
        // Delete button
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(Icons.Rounded.Delete, contentDescription = "Elimina", tint = Color(0xFFE5474B))
        }
    }
}

@Composable
private fun EmptyHostState(onCreateProperty: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(Primario.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Home, null, tint = Primario, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Nessun annuncio pubblicato", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF222222))
            Spacer(Modifier.height(8.dp))
            Text(
                "Pubblica la tua prima proprietà e inizia a guadagnare.",
                fontSize = 14.sp,
                color = CaptionLabels,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCreateProperty,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aggiungi proprietà", fontWeight = FontWeight.Bold)
            }
        }
    }
}
