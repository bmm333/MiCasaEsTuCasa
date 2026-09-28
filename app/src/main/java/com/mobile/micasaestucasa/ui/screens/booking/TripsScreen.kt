package com.mobile.micasaestucasa.ui.screens.booking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaConnectedBottomNav
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingUiState
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private data class TabItem(val label: String, val count: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProperty: (String) -> Unit,
    onNavigateToChat: (hostId: String, renterId: String, propertyId: String) -> Unit,
    navController: NavController,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewedIds by viewModel.reviewedBookingIds.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadRenterBookings(currentUserId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    LaunchedEffect(uiState) {
        if (uiState is BookingUiState.ActionSuccess) viewModel.loadRenterBookings(currentUserId)
    }
    val allBookings = (uiState as? BookingUiState.BookingsLoaded)?.bookings ?: emptyList()
    val today = remember { LocalDate.now().toString() }

    val upcoming = remember(allBookings) {
        allBookings.filter {
            it.status == BookingStatus.REQUESTED ||
            (it.status == BookingStatus.ACCEPTED && it.startDate >= today)
        }.sortedBy { it.startDate }
    }
    val active = remember(allBookings) {
        allBookings.filter {
            it.status == BookingStatus.ACCEPTED && it.startDate <= today && it.endDate >= today
        }.sortedBy { it.endDate }
    }
    val past = remember(allBookings) {
        allBookings.filter {
            it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED ||
            it.status == BookingStatus.REJECTED ||
            (it.status == BookingStatus.ACCEPTED && it.endDate < today)
        }.sortedByDescending { it.endDate }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("I miei viaggi", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        bottomBar = {
            MiCasaConnectedBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "trips_screen",
                onItemSelected = { route ->
                    when (route) {
                        "home_screen" -> navController.navigate(Route.Home) { popUpTo(0) }
                        "saved_screen" -> navController.navigate(Route.Wishlist)
                        "trips_screen" -> {}
                        "messages_screen" -> navController.navigate(Route.ConversationList)
                        "profile_screen" -> navController.navigate(Route.Profile)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PillTabRow(
                tabs = listOf(TabItem("In arrivo", upcoming.size), TabItem("In corso", active.size), TabItem("Passati", past.size)),
                selectedIndex = pagerState.currentPage,
                onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } }
            )
            when (uiState) {
                is BookingUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Primario) }
                is BookingUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text((uiState as BookingUiState.Error).message, color = ErrorColor) }
                else -> HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) { page ->
                    when (page) {
                                0 -> BookingTab(upcoming, Icons.Rounded.Luggage, "Nessun viaggio in arrivo", "I tuoi prossimi soggiorni appariranno qui") { booking ->
                            UpcomingCard(booking,
                                onCancel = { viewModel.cancelBooking(booking.id, currentUserId) },
                                onChat = { onNavigateToChat(booking.hostId, currentUserId, booking.propertyId) },
                                onClick = { onNavigateToProperty(booking.propertyId) },
                                onEdit = { newStart, newEnd, newGuests ->
                                    viewModel.updateBooking(
                                        bookingId = booking.id,
                                        renterId = currentUserId,
                                        newStartDate = newStart,
                                        newEndDate = newEnd,
                                        newGuestsCount = newGuests,
                                        pricePerDay = booking.pricePerDay
                                    )
                                }
                            )
                        }
                        1 -> BookingTab(active, Icons.Rounded.Home, "Nessun soggiorno in corso", "I soggiorni attivi appariranno qui") { booking ->
                            ActiveCard(booking, onChat = { onNavigateToChat(booking.hostId, currentUserId, booking.propertyId) },
                                onClick = { onNavigateToProperty(booking.propertyId) })
                        }
                        2 -> PastTab(
                            past,
                            onNavigateToProperty,
                            reviewedIds,
                            onNavigateToReview = { b ->
                                navController.navigate(
                                    Route.WriteReview(
                                        bookingId = b.id,
                                        propertyId = b.propertyId,
                                        hostId = b.hostId,
                                        renterId = b.renterId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillTabRow(tabs: List<TabItem>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val bgColor by animateColorAsState(if (isSelected) Primario else SkeletonLoader, tween(200), label = "tabBg")
            val textColor by animateColorAsState(if (isSelected) CardSurface else CaptionLabels, tween(200), label = "tabText")
            Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50.dp), color = bgColor) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(tab.label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = textColor)
                    if (tab.count > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(18.dp).clip(RoundedCornerShape(50.dp)).background(if (isSelected) CardSurface.copy(alpha = 0.3f) else Primario.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text(
                                text = "${tab.count}", 
                                fontSize = 10.sp, 
                                color = if (isSelected) CardSurface else Primario, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> BookingTab(items: List<T>, emptyIcon: ImageVector, emptyTitle: String, emptySubtitle: String, content: @Composable (T) -> Unit) {
    if (items.isEmpty()) {
        EmptyTabView(emptyIcon, emptyTitle, emptySubtitle)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item -> content(item) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpcomingCard(
    booking: Booking,
    onCancel: () -> Unit,
    onChat: () -> Unit,
    onClick: () -> Unit,
    onEdit: (newStart: String, newEnd: String, newGuests: Int) -> Unit = { _, _, _ -> }
) {
    val daysUntil = remember(booking.startDate) {
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(booking.startDate)).toInt() } catch (_: Exception) { 0 }
    }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditBookingDialog(
            booking = booking,
            onDismiss = { showEditDialog = false },
            onConfirm = { newStart, newEnd, newGuests ->
                onEdit(newStart, newEnd, newGuests)
                showEditDialog = false
            }
        )
    }

    if (showCancelDialog) {
        RefundCancelDialog(
            totalPrice = booking.totalPrice,
            onConfirm = {
                showCancelDialog = false
                onCancel()
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardSurface)) {
        Box(Modifier.fillMaxWidth().background(when { daysUntil <= 3 -> Primario; daysUntil <= 7 -> Caution; else -> Secondary }).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(when { daysUntil == 0 -> "Oggi!"; daysUntil == 1 -> "Domani!"; else -> "Tra $daysUntil giorni" }, color = CardSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                StatusBadgeSmall(booking.status)
            }
        }
        Column(Modifier.padding(16.dp)) {
            DateRow(booking)
            Spacer(Modifier.height(6.dp))
            GuestsPriceRow(booking)
            if (booking.status == BookingStatus.REQUESTED || booking.status == BookingStatus.ACCEPTED) {
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = BorderDivider); Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onChat, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario), border = BorderStroke(1.dp, Primario)) {
                        Icon(Icons.Rounded.Chat, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                        Text("Contatta host", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (booking.status == BookingStatus.REQUESTED) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor), border = BorderStroke(1.dp, ErrorColor)) {
                            Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                            Text("Cancella", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                // bottone modifica: solo per booking REQUESTED
                if (booking.status == BookingStatus.REQUESTED) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
                        border = BorderStroke(1.dp, Secondary)
                    ) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Modifica prenotazione", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                // bottone cancella con conferma rimborso: solo per booking ACCEPTED
                if (booking.status == BookingStatus.ACCEPTED) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                        border = BorderStroke(1.dp, ErrorColor)
                    ) {
                        Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancella prenotazione", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveCard(booking: Booking, onChat: () -> Unit, onClick: () -> Unit) {
    val daysLeft = remember(booking.endDate) {
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(booking.endDate)).toInt() } catch (_: Exception) { 0 }
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardSurface)) {
        Row(Modifier.fillMaxWidth().background(Secondary).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(50.dp)).background(CardSurface))
                Spacer(Modifier.width(8.dp))
                Text("In corso", color = CardSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(if (daysLeft == 0) "Ultimo giorno!" else "Ancora $daysLeft ${if (daysLeft == 1) "giorno" else "giorni"}", color = CardSurface, fontSize = 13.sp)
        }
        Column(Modifier.padding(16.dp)) {
            DateRow(booking); Spacer(Modifier.height(6.dp))
            Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HeadingText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = BorderDivider); Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onChat, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario), border = BorderStroke(1.dp, Primario)) {
                    Icon(Icons.Rounded.Chat, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                    Text("Contatta host", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = onClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)) {
                    Icon(Icons.Rounded.Home, null, tint = CardSurface, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                    Text("Vedi casa", fontSize = 13.sp, color = CardSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PastTab(
    bookings: List<Booking>,
    onNavigateToProperty: (String) -> Unit,
    reviewedIds: Set<String>,
    onNavigateToReview: (Booking) -> Unit
) {
    if (bookings.isEmpty()) {
        EmptyTabView(Icons.Rounded.History, "Nessun viaggio passato", "I tuoi soggiorni completati appariranno qui")
        return
    }
    val completed = bookings.filter { it.status == BookingStatus.COMPLETED }.sortedByDescending { it.endDate }
    val cancelled = bookings.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }.sortedByDescending { it.endDate }

    LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (completed.isNotEmpty()) {
            item { Text("Soggiorni completati", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText, modifier = Modifier.padding(bottom = 4.dp)) }
            items(completed) { booking -> 
                CompletedCard(
                    booking = booking,
                    hasReviewed = booking.id in reviewedIds,
                    onClick = { onNavigateToProperty(booking.propertyId) },
                    onReview = { onNavigateToReview(booking) }
                ) 
            }
        }
        if (cancelled.isNotEmpty()) {
            item { Spacer(Modifier.height(8.dp)); Text("Cancellate / Rifiutate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CaptionLabels, modifier = Modifier.padding(bottom = 4.dp)) }
            items(cancelled) { booking -> CancelledCard(booking) }
        }
    }
}

@Composable
private fun CompletedCard(
    booking: Booking,
    hasReviewed: Boolean,
    onClick: () -> Unit,
    onReview: () -> Unit
) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardSurface)) {
        Row(Modifier.fillMaxWidth().background(Success).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CheckCircle, null, tint = Badges, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Completato", color = Badges, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Column(Modifier.padding(16.dp)) {
            DateRow(booking); Spacer(Modifier.height(4.dp))
            Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = HeadingText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = BorderDivider); Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CaptionLabels),
                    border = BorderStroke(1.dp, BorderDivider)
                ) {
                    Text("Vedi casa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                if (!hasReviewed) {
                    Button(
                        onClick = onReview,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Text("Recensisci", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelledCard(booking: Booking) {
    val isRejected = booking.status == BookingStatus.REJECTED
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardSurface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (isRejected) ErrorColor.copy(alpha = 0.1f) else SkeletonLoader), contentAlignment = Alignment.Center) {
            Icon(if (isRejected) Icons.Rounded.Block else Icons.Rounded.Cancel, null, tint = if (isRejected) ErrorColor else CaptionLabels, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (isRejected) "Rifiutata dall'host" else "Cancellata", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (isRejected) ErrorColor else CaptionLabels)
            Text("${booking.startDate} -> ${booking.endDate}", fontSize = 12.sp, color = CaptionLabels)
        }
        Text("${booking.totalPrice.toInt()} EUR", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CaptionLabels)
    }
}

@Composable private fun DateRow(booking: Booking) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CalendarMonth, null, tint = Primario, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("${booking.startDate} -> ${booking.endDate}", fontSize = 14.sp, color = HeadingText, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun GuestsPriceRow(booking: Booking) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Rounded.People, null, tint = CaptionLabels, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("${booking.guestsCount} ospiti", fontSize = 13.sp, color = CaptionLabels)
        }
        Spacer(Modifier.height(8.dp))
        Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HeadingText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable private fun StatusBadgeSmall(status: BookingStatus) {
    val label = when (status) { BookingStatus.REQUESTED -> "In attesa"; BookingStatus.ACCEPTED -> "Confermato"; else -> return }
    Surface(shape = RoundedCornerShape(50.dp), color = CardSurface.copy(alpha = 0.25f)) {
        Text(label, fontSize = 11.sp, color = CardSurface, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable private fun EmptyTabView(icon: ImageVector, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = BorderDivider, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HeadingText, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, fontSize = 14.sp, color = CaptionLabels, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RefundCancelDialog(
    totalPrice: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurface)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icona avviso
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(ErrorColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                "Cancella prenotazione",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = HeadingText,
                textAlign = TextAlign.Center
            )

            Text(
                "Stai per cancellare una prenotazione già confermata dall'host.\n\nLa prenotazione verrà eliminata e riceverai un rimborso completo.",
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // Importo rimborso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Success.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rimborso previsto", fontSize = 14.sp, color = HeadingText, fontWeight = FontWeight.Medium)
                Text(
                    "€${totalPrice.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary
                )
            }

            HorizontalDivider(color = BorderDivider)

            // Bottoni
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Sì, cancella prenotazione", fontWeight = FontWeight.SemiBold, color = CardSurface)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeadingText),
                    border = BorderStroke(1.dp, BorderDivider)
                ) {
                    Text("Torna indietro", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirm: (newStart: String, newEnd: String, newGuests: Int) -> Unit
) {
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(booking.startDate)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) { null }
    )
    val endPickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(booking.endDate)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) { null }
    )
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var guests by remember { mutableIntStateOf(booking.guestsCount) }

    val startDate = startPickerState.selectedDateMillis?.let { millisToIsoDate(it) } ?: booking.startDate
    val endDate = endPickerState.selectedDateMillis?.let { millisToIsoDate(it) } ?: booking.endDate
    val nights = try {
        val s = LocalDate.parse(startDate); val e = LocalDate.parse(endDate)
        ChronoUnit.DAYS.between(s, e).toInt().coerceAtLeast(0)
    } catch (_: Exception) { 0 }
    val newTotal = nights * booking.pricePerDay

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = { TextButton(onClick = { showStartPicker = false }) { Text("OK", color = Primario) } },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Annulla", color = CaptionLabels) } }
        ) {
            DatePicker(
                state = startPickerState,
                colors = DatePickerDefaults.colors(selectedDayContainerColor = Primario, todayDateBorderColor = Primario)
            )
        }
    }
    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = { TextButton(onClick = { showEndPicker = false }) { Text("OK", color = Primario) } },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Annulla", color = CaptionLabels) } }
        ) {
            DatePicker(
                state = endPickerState,
                colors = DatePickerDefaults.colors(selectedDayContainerColor = Primario, todayDateBorderColor = Primario)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Modifica prenotazione", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HeadingText)

            // Date buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Check-in
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ScreenBackground)
                        .clickable { showStartPicker = true }
                        .padding(12.dp)
                ) {
                    Text("Check-in", fontSize = 11.sp, color = CaptionLabels, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(startDate, fontSize = 14.sp, color = HeadingText, fontWeight = FontWeight.SemiBold)
                }
                // Check-out
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ScreenBackground)
                        .clickable { showEndPicker = true }
                        .padding(12.dp)
                ) {
                    Text("Check-out", fontSize = 11.sp, color = CaptionLabels, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(endDate, fontSize = 14.sp, color = HeadingText, fontWeight = FontWeight.SemiBold)
                }
            }

            if (nights > 0) {
                Text("$nights ${if (nights == 1) "notte" else "notti"} · €${newTotal.toInt()} totale",
                    fontSize = 13.sp, color = Secondary, fontWeight = FontWeight.Medium)
            }

            // Guests stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ospiti", fontSize = 15.sp, color = HeadingText, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (guests > 1) guests-- },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50.dp)).background(SkeletonLoader)
                    ) { Icon(Icons.Rounded.Remove, null, tint = HeadingText, modifier = Modifier.size(16.dp)) }
                    Text("$guests", modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = HeadingText)
                    IconButton(
                        onClick = { guests++ },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50.dp)).background(Primario)
                    ) { Icon(Icons.Rounded.Add, null, tint = CardSurface, modifier = Modifier.size(16.dp)) }
                }
            }

            HorizontalDivider(color = BorderDivider)

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CaptionLabels),
                    border = BorderStroke(1.dp, BorderDivider)
                ) { Text("Annulla", fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = { if (nights > 0) onConfirm(startDate, endDate, guests) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    enabled = nights > 0
                ) { Text("Salva", fontWeight = FontWeight.SemiBold, color = CardSurface) }
            }
        }
    }
}

private fun millisToIsoDate(millis: Long): String {
    return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
}

