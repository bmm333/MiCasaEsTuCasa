package com.mobile.micasaestucasa.ui.screens.booking

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
 * Booking request screen handles data selection, guests count , effective cost, and mocked paayment
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(
    propertyId:String,
    propertyTitle:String,
    pricePerDay: Double,
    hostId:String,
    currentUserId:String,
    onNavigateBack:()->Unit,
    onBookingSuccess:()-> Unit,
    viewModel: BookingViewModel = hiltViewModel()
)
{
    val uiState by viewModel.uiState.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var guestCount by remember { androidx.compose.runtime.mutableIntStateOf(1) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    // total nights calc
    val nights = remember(startDate, endDate) { calculateNights(startDate, endDate) }
    val totalprice = nights * pricePerDay
    LaunchedEffect(uiState,paymentState) {
        if (uiState is BookingUiState.BookingCreated && paymentState is PaymentUiStatus.Success)
        {
            onBookingSuccess()
        }
    }
    LaunchedEffect(uiState) {
        if(uiState is BookingUiState.BookingCreated)
            showPaymentDialog=true
    }
    if (showPaymentDialog) {
        MockPaymentDialog(
            totalPrice   = totalprice,
            paymentState = paymentState,
            onConfirm    = {
                val bookingId = (uiState as? BookingUiState.BookingCreated)?.bookingId ?: ""
                viewModel.processPayment(bookingId)
            },
            onDismiss    = { showPaymentDialog = false }
        )
    }
    Scaffold(
        containerColor=ScreenBackground,
        topBar={
            TopAppBar(
                title={
                    Text(
                        "Request Booking",
                        fontWeight= FontWeight.Bold,
                        fontSize=18.sp,
                        color=HeadingText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick=onNavigateBack
                    ){
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = HeadingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    )
    {
        padding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal=20.dp,vertical=16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //selected prop
            SectionCard(title="Property")
            {
                Text(
                    text=propertyTitle,
                    fontWeight= FontWeight.Bold,
                    fontSize=16.sp,
                    color=HeadingText
                )
                Text(
                    text = "€${pricePerDay.toInt()} /night",
                    fontSize = 14.sp,
                    color = CaptionLabels
                )
            }
            //pciker of date
            SectionCard(title = "Date soggiorno") {
                DateInputRow(
                    label    = "Check-in",
                    value    = startDate,
                    onChange = { startDate = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                DateInputRow(
                    label    = "Check-out",
                    value    = endDate,
                    onChange = { endDate = it }
                )
                if (nights > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text     = "$nights nights selected",
                        fontSize = 13.sp,
                        color    = Secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            //guests count
            SectionCard(title = "Guests") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement= Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Guests Count", fontSize=15.sp,color=HeadingText)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (guestCount > 1) guestCount-- },
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
                            text = "$guestCount",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = HeadingText
                        )
                        IconButton(
                            onClick = { guestCount++ },
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
            if(nights>0)
            {
                SectionCard(title="Details")
                {
                    PriceRow("€${pricePerDay.toInt()} x $nights nights", "€${(pricePerDay * nights).toInt()}")
                    HorizontalDivider(color = BorderDivider, modifier = Modifier.padding(vertical = 8.dp))
                    PriceRow("Total", "€${totalprice.toInt()}", bold = true)
                }
            }
            if (uiState is BookingUiState.Error) {
                Text(
                    text     = (uiState as BookingUiState.Error).message,
                    color    = ErrorColor,
                    fontSize = 13.sp
                )
            }
            MiCasaPrimaryButton(
                text = "Richiedi prenotazione",
                onClick = {
                    val booking = Booking(
                        id = "",
                        propertyId = propertyId,
                        renterId = currentUserId,
                        hostId = hostId,
                        startDate = startDate,
                        endDate = endDate,
                        guestsCount = guestCount,
                        pricePerDay = pricePerDay,
                        totalPrice = totalprice,
                        status = BookingStatus.REQUESTED,
                        idempotencyKey = ""
                    )
                    viewModel.createBooking(booking)
                },
                enabled = uiState !is BookingUiState.Loading &&
                    startDate.isNotBlank() && endDate.isNotBlank() && nights > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
private fun DateInputRow(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, color = CaptionLabels) },
        placeholder   = { Text("AAAA-MM-GG", color = BorderDivider) },
        leadingIcon   = {
            Icon(Icons.Rounded.CalendarMonth, null, tint = Primario,
                modifier = Modifier.size(20.dp))
        },
        shape  = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primario,
            unfocusedBorderColor = BorderDivider,
            focusedLabelColor    = Primario,
            cursorColor          = Primario,
            focusedContainerColor   = CardSurface,
            unfocusedContainerColor = CardSurface
        ),
        modifier  = Modifier.fillMaxWidth(),
        singleLine = true
    )
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
private fun calculateNights(start: String, end: String): Int {
    return try {
        val s = java.time.LocalDate.parse(start)
        val e = java.time.LocalDate.parse(end)
        java.time.temporal.ChronoUnit.DAYS.between(s, e).toInt().coerceAtLeast(0)
    } catch (e: Exception) { 0 }
}