package com.mobile.micasaestucasa.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingUiState
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingViewModel

import androidx.navigation.NavController
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.navigation.Route

/**
 * Booking list mode — determines which bookings to load.
 * The mode is set by the navigation route.
 * */
enum class BookingListMode { RENTER, HOST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    currentUserId: String,
    mode: BookingListMode,
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: BookingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        when (mode) {
            BookingListMode.RENTER -> viewModel.loadRenterBookings(currentUserId)
            BookingListMode.HOST   -> viewModel.loadHostBookings(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (mode) {
                            BookingListMode.RENTER -> "I miei viaggi"
                            BookingListMode.HOST   -> "Le mie prenotazioni ricevute"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = HeadingText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = if (mode == BookingListMode.RENTER) "trips_screen" else "profile_screen",
                onItemSelected = { route ->
                    when (route) {
                        "home_screen"     -> navController.navigate(Route.Home) {
                            popUpTo(0)
                        }
                        "saved_screen"    -> navController.navigate(Route.Wishlist)
                        "trips_screen"    -> {
                            if (mode != BookingListMode.RENTER) {
                                navController.navigate(Route.Trips)
                            }
                        }
                        "messages_screen" -> navController.navigate(Route.ConversationList)
                        "profile_screen"  -> {
                            if (mode == BookingListMode.RENTER) {
                                navController.navigate(Route.Profile)
                            } else {
                                navController.navigate(Route.Profile) {
                                    popUpTo(Route.Profile) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        val uiState by viewModel.uiState.collectAsState()
        when (val state = uiState) {
            is BookingUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Primario)
                }
            }
            is BookingUiState.BookingsLoaded -> {
                if (state.bookings.isEmpty()) {
                    EmptyBookingsView(
                        modifier = Modifier.padding(padding),
                        mode     = mode
                    )
                } else {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.bookings) { booking ->
                            BookingCard(
                                booking  = booking,
                                isHost   = mode == BookingListMode.HOST,
                                onAccept = { viewModel.acceptBooking(booking.id, currentUserId) },
                                onCancel = { viewModel.cancelBooking(booking.id, currentUserId) }
                            )
                        }
                    }
                }
            }
            is BookingUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(state.message, color = ErrorColor)
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isHost: Boolean,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .padding(16.dp)
    ) {
        // header: property id + status badge
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Bookings",
                    fontSize   = 12.sp,
                    color      = CaptionLabels
                )
                Text(
                    text       = booking.propertyId.take(8) + "...",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = HeadingText
                )
            }
            BookingStatusBadge(booking.status)
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = BorderDivider)
        Spacer(modifier = Modifier.height(12.dp))

        // date and guests
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            InfoItem(Icons.Rounded.CalendarMonth,
                "${booking.startDate} → ${booking.endDate}")
            InfoItem(Icons.Rounded.People, "${booking.guestsCount} guests")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // total price
        Text(
            text       = "Total: €${booking.totalPrice.toInt()}",
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp,
            color      = HeadingText
        )

        // host action accept/reject if req
        if (isHost && booking.status == BookingStatus.REQUESTED) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onCancel,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor)
                ) {
                    Text("Reject", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick  = onAccept,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Text("Accept", fontWeight = FontWeight.SemiBold, color = CardSurface)
                }
            }
        }

        // renter action: cancel if req
        if (!isHost && booking.status == BookingStatus.REQUESTED) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                border   = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor)
            ) {
                Text("Cancel Booking", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BookingStatusBadge(status: BookingStatus) {
    val (label, bg, textColor) = when (status) {
        BookingStatus.REQUESTED  -> Triple("On Hold",   Caution.copy(alpha = 0.15f),  Caution)
        BookingStatus.ACCEPTED   -> Triple("Accepted",  Success,                       Secondary)
        BookingStatus.REJECTED   -> Triple("Rejected",  ErrorColor.copy(alpha = 0.1f), ErrorColor)
        BookingStatus.CANCELLED  -> Triple("Cancelled", SkeletonLoader,                CaptionLabels)
        BookingStatus.COMPLETED  -> Triple("Completed", Success,                       Badges)
    }
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = bg
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textColor,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primario, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 13.sp, color = SecondaryText)
    }
}

@Composable
private fun EmptyBookingsView(modifier: Modifier = Modifier, mode: BookingListMode) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.CalendarMonth, null,
                tint     = BorderDivider,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (mode) {
                    BookingListMode.RENTER -> "Nessun viaggio ancora"
                    BookingListMode.HOST   -> "Nessuna prenotazione ricevuta"
                },
                color    = CaptionLabels,
                fontSize = 16.sp
            )
        }
    }
}