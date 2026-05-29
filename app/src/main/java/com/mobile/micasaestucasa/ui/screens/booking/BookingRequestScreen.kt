package com.mobile.micasaestucasa.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.ui.components.atomics.MiCasaPrimaryButton
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.SecondaryText
import com.mobile.micasaestucasa.ui.theme.SkeletonLoader
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingUiState
import com.mobile.micasaestucasa.ui.viewmodels.booking.BookingViewModel
import com.mobile.micasaestucasa.ui.viewmodels.booking.PaymentUiStatus

/**
 * Booking request screen handles date selection, guests count, effective cost, and mocked payment.
 *
 * Dates are selected via Material3 DatePickerDialog (no manual text input).
 * Payment is a mock flow with a 2-second simulated delay — no real payment provider is integrated.
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(
    propertyId: String,
    propertyTitle: String,
    pricePerDay: Double,
    hostId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val unavailableDates by viewModel.unavailableDatesMillis.collectAsState()
    // Load booked dates for this property so the calendar disables them
    LaunchedEffect(propertyId) {
        viewModel.loadUnavailableDates(propertyId)
    }
    //Custom SelectableDates: disables past and already-booked days
    val selectableDates = remember(unavailableDates) {
        object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Block past dates
                val today = java.time.LocalDate.now()
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant().toEpochMilli()
                if (utcTimeMillis < today) return false
                //Block booked dates
                return utcTimeMillis !in unavailableDates
            }
        }
    }

    // DatePicker state
    val startDatePickerState = rememberDatePickerState(selectableDates = selectableDates)
    val endDatePickerState   = rememberDatePickerState(selectableDates = selectableDates)
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }
    var guestsCount     by remember { mutableIntStateOf(1) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val startDate = startDatePickerState.selectedDateMillis?.let {
        millisToIso(it)
    } ?: ""
    val endDate = endDatePickerState.selectedDateMillis?.let {
        millisToIso(it)
    } ?: ""

    val nights     = calculateNights(startDate, endDate)
    val totalPrice = nights * pricePerDay

    LaunchedEffect(uiState) {
        if (uiState is BookingUiState.BookingCreated) showPaymentDialog = true
    }
    LaunchedEffect(uiState, paymentState) {
        if (uiState is BookingUiState.BookingCreated &&
            paymentState is PaymentUiStatus.Success) onBookingSuccess()
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("OK", color = Primario)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Annulla", color = CaptionLabels)
                }
            }
        ) {
            DatePicker(
                state  = startDatePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Primario,
                    todayDateBorderColor      = Primario
                )
            )
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("OK", color = Primario)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Annulla", color = CaptionLabels)
                }
            }
        ) {
            DatePicker(
                state  = endDatePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Primario,
                    todayDateBorderColor      = Primario
                )
            )
        }
    }

    if (showPaymentDialog) {
        MockPaymentDialog(
            totalPrice   = totalPrice,
            paymentState = paymentState,
            onConfirm    = {
                val bookingId = (uiState as? BookingUiState.BookingCreated)?.bookingId ?: ""
                viewModel.processPayment(bookingId)
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prenota",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeadingText
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Property info
            SectionCard("Proprietà") {
                Text(
                    propertyTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = HeadingText
                )
                Text(
                    "€${pricePerDay.toInt()} / notte",
                    fontSize = 14.sp,
                    color = CaptionLabels
                )
            }

            SectionCard("Quando") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateButton(
                        label    = "Check-in",
                        value    = if (startDate.isBlank()) "Seleziona" else startDate,
                        onClick  = { showStartPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DateButton(
                        label    = "Check-out",
                        value    = if (endDate.isBlank()) "Seleziona" else endDate,
                        onClick  = { showEndPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (nights > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$nights ${if (nights == 1) "notte" else "notti"}",
                        fontSize   = 13.sp,
                        color      = Secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Guests count
            SectionCard("Ospiti") {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Ospiti",
                            fontSize = 15.sp,
                            color = HeadingText,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Max capacità selezionata",
                            fontSize = 12.sp,
                            color = CaptionLabels
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick  = { if (guestsCount > 1) guestsCount-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(SkeletonLoader)
                        ) {
                            Icon(
                                Icons.Rounded.Remove, null,
                                tint = HeadingText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "$guestsCount",
                            modifier   = Modifier.padding(horizontal = 20.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 20.sp,
                            color      = HeadingText
                        )
                        IconButton(
                            onClick  = { guestsCount++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Primario)
                        ) {
                            Icon(
                                Icons.Rounded.Add, null,
                                tint = CardSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Price summary
            if (nights > 0) {
                SectionCard("Riepilogo") {
                    PriceRow(
                        "€${pricePerDay.toInt()} × $nights notti",
                        "€${(pricePerDay * nights).toInt()}"
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = BorderDivider)
                    Spacer(Modifier.height(8.dp))
                    PriceRow("Totale", "€${totalPrice.toInt()}", bold = true)
                }
            }

            if (uiState is BookingUiState.Error) {
                Text(
                    (uiState as BookingUiState.Error).message,
                    color = ErrorColor, fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            MiCasaPrimaryButton(
                text = "Richiedi prenotazione",
                onClick = {
                    val booking = Booking(
                        id             = "",
                        propertyId     = propertyId,
                        renterId       = currentUserId,
                        hostId         = hostId,
                        startDate      = startDate,
                        endDate        = endDate,
                        guestsCount    = guestsCount,
                        pricePerDay    = pricePerDay,
                        totalPrice     = totalPrice,
                        status         = BookingStatus.REQUESTED,
                        idempotencyKey = ""
                    )
                    viewModel.createBooking(booking)
                },
                enabled = startDate.isNotBlank() && endDate.isNotBlank() && nights > 0
                        && uiState !is BookingUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DateButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ScreenBackground)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = CaptionLabels,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontSize   = 14.sp,
            color      = if (value == "Seleziona") BorderDivider else HeadingText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .padding(16.dp)
    ) {
        Text(
            text       = title,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp,
            color      = HeadingText
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun PriceRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            color      = if (bold) HeadingText else SecondaryText,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text       = value,
            fontSize   = 14.sp,
            color      = HeadingText,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MockPaymentDialog(
    totalPrice: Double,
    paymentState: PaymentUiStatus,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardSurface,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text("Confirm payment", fontWeight = FontWeight.Bold, color = HeadingText)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (paymentState) {
                    is PaymentUiStatus.Processing -> {
                        CircularProgressIndicator(color = Primario)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Payment processing...", color = CaptionLabels)
                    }
                    else -> {
                        Icon(
                            Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint     = Primario,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Total to be paid",
                            fontSize = 14.sp,
                            color    = CaptionLabels
                        )
                        Text(
                            "€${totalPrice.toInt()}",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = HeadingText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Demo — no real payment",
                            fontSize = 11.sp,
                            color    = CaptionLabels
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (paymentState !is PaymentUiStatus.Processing) {
                MiCasaPrimaryButton(
                    text     = "Pay now",
                    onClick  = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}


private fun millisToIso(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ITALY)
    return sdf.format(java.util.Date(millis))
}


private fun calculateNights(start: String, end: String): Int {
    return try {
        val s = java.time.LocalDate.parse(start)
        val e = java.time.LocalDate.parse(end)
        java.time.temporal.ChronoUnit.DAYS.between(s, e).toInt().coerceAtLeast(0)
    } catch (e: Exception) { 0 }
}