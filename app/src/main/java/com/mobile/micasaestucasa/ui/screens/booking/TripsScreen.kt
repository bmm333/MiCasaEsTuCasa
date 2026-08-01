package com.mobile.micasaestucasa.ui.screens.booking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.navigation.Route
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingUiState
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadRenterBookings(currentUserId) }
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
            MiCasaBottomNav(
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
                else -> HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> BookingTab(upcoming, Icons.Rounded.Luggage, "Nessun viaggio in arrivo", "I tuoi prossimi soggiorni appariranno qui") { booking ->
                            UpcomingCard(booking, onCancel = { viewModel.cancelBooking(booking.id, currentUserId) },
                                onChat = { onNavigateToChat(booking.hostId, currentUserId, booking.propertyId) },
                                onClick = { onNavigateToProperty(booking.propertyId) })
                        }
                        1 -> BookingTab(active, Icons.Rounded.Home, "Nessun soggiorno in corso", "I soggiorni attivi appariranno qui") { booking ->
                            ActiveCard(booking, onChat = { onNavigateToChat(booking.hostId, currentUserId, booking.propertyId) },
                                onClick = { onNavigateToProperty(booking.propertyId) })
                        }
                        2 -> PastTab(past, onNavigateToProperty)
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
                            Text("${tab.count}", fontSize = 10.sp, color = if (isSelected) CardSurface else Primario, fontWeight = FontWeight.Bold)
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

@Composable
private fun UpcomingCard(booking: Booking, onCancel: () -> Unit, onChat: () -> Unit, onClick: () -> Unit) {
    val daysUntil = remember(booking.startDate) {
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(booking.startDate)).toInt() } catch (_: Exception) { 0 }
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
            Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HeadingText)
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
private fun PastTab(bookings: List<Booking>, onNavigateToProperty: (String) -> Unit) {
    if (bookings.isEmpty()) {
        EmptyTabView(Icons.Rounded.History, "Nessun viaggio passato", "I tuoi soggiorni completati appariranno qui")
        return
    }
    val completed = bookings.filter { it.status == BookingStatus.COMPLETED || (it.status == BookingStatus.ACCEPTED && it.endDate < LocalDate.now().toString()) }
    val cancelled = bookings.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (completed.isNotEmpty()) {
            item { Text("Soggiorni completati", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText, modifier = Modifier.padding(bottom = 4.dp)) }
            items(completed) { booking -> CompletedCard(booking, onClick = { onNavigateToProperty(booking.propertyId) }) }
        }
        if (cancelled.isNotEmpty()) {
            item { Spacer(Modifier.height(8.dp)); Text("Cancellate / Rifiutate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CaptionLabels, modifier = Modifier.padding(bottom = 4.dp)) }
            items(cancelled) { booking -> CancelledCard(booking) }
        }
    }
}

@Composable
private fun CompletedCard(booking: Booking, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardSurface)) {
        Row(Modifier.fillMaxWidth().background(Success).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CheckCircle, null, tint = Badges, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Completato", color = Badges, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Column(Modifier.padding(16.dp)) {
            DateRow(booking); Spacer(Modifier.height(4.dp))
            Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = HeadingText)
            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = BorderDivider); Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CaptionLabels), border = BorderStroke(1.dp, BorderDivider)) {
                Text("Vedi casa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.People, null, tint = CaptionLabels, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("${booking.guestsCount} ospiti", fontSize = 13.sp, color = CaptionLabels)
        }
        Text("${booking.totalPrice.toInt()} EUR totale", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HeadingText)
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
