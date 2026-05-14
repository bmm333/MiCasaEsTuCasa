package com.mobile.micasaestucasa.ui.screens.booking


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    currentUserId: String,
    isHost: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Come affittuario", "Come proprietario")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) viewModel.loadRenterBookings(currentUserId)
        else viewModel.loadHostBookings(currentUserId)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Le mie prenotazioni",
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HeadingText)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // tab renter / host
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = CardSurface,
                contentColor     = Primario,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primario
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                title,
                                fontSize   = 13.sp,
                                fontWeight = if (selectedTab == index)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) Primario else CaptionLabels
                            )
                        }
                    )
                }
            }

            when (val state = uiState) {
                is BookingUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primario)
                    }
                }
                is BookingUiState.BookingsLoaded -> {
                    if (state.bookings.isEmpty()) {
                        EmptyBookingsView()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.bookings) { booking ->
                                BookingCard(
                                    booking    = booking,
                                    isHost     = selectedTab == 1,
                                    onAccept   = { viewModel.acceptBooking(booking.id, currentUserId) },
                                    onCancel   = { viewModel.cancelBooking(booking.id, currentUserId) }
                                )
                            }
                        }
                    }
                }
                is BookingUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = ErrorColor)
                    }
                }
                else -> {}
            }
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

        // renter action: cacnel if req
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
        BookingStatus.ACCEPTED   -> Triple("Accpeted",   Success,                       Secondary)
        BookingStatus.REJECTED   -> Triple("Rejected",   ErrorColor.copy(alpha = 0.1f), ErrorColor)
        BookingStatus.CANCELLED  -> Triple("Cancelled",  SkeletonLoader,                CaptionLabels)
        BookingStatus.COMPLETED  -> Triple("Completed",  Success,                       Badges)
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
private fun EmptyBookingsView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.CalendarMonth,
                null, tint = BorderDivider,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Bookings", color = CaptionLabels, fontSize = 16.sp)
        }
    }
}