package com.mobile.micasaestucasa.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.ui.theme.*
import com.mobile.micasaestucasa.ui.viewmodels.review.ReviewUiState
import com.mobile.micasaestucasa.ui.viewmodels.review.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    bookingId: String,
    propertyId: String,
    hostId: String,
    renterId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    var rating by remember { mutableIntStateOf(0) }
    var hostRating by remember { mutableIntStateOf(0) }
    var reviewBody by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val isHostReviewingRenter = currentUserId == hostId
    val reviewType = if (isHostReviewingRenter) ReviewType.RENTER_REVIEW else ReviewType.PROPERTY_REVIEW
    val targetId = if (isHostReviewingRenter) renterId else propertyId

    LaunchedEffect(uiState) {
        if (uiState is ReviewUiState.ReviewSubmitted) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lascia una recensione", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isHostReviewingRenter) {
                Text(
                    text = "Come è stato l'ospite?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Rating Stars
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Caution else CaptionLabels,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Come valuti la struttura?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingText
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Caution else CaptionLabels,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Come valuti l'host?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingText
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        IconButton(onClick = { hostRating = i }) {
                            Icon(
                                imageVector = if (i <= hostRating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Host Star $i",
                                tint = if (i <= hostRating) Caution else CaptionLabels,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = reviewBody,
                onValueChange = { reviewBody = it },
                label = { Text("Scrivi la tua esperienza...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primario,
                    unfocusedBorderColor = BorderDivider,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                ),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState is ReviewUiState.Error) {
                Text(
                    text = (uiState as ReviewUiState.Error).message,
                    color = ErrorColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val isValid = if (isHostReviewingRenter) rating > 0 else (rating > 0 && hostRating > 0)
                    if (isValid) {
                        val review = Review(
                            bookingId = bookingId,
                            propertyId = propertyId,
                            authorId = currentUserId,
                            targetId = targetId,
                            reviewType = reviewType,
                            stars = rating,
                            hostStars = if (!isHostReviewingRenter) hostRating else null,
                            body = reviewBody,
                            title = "Recensione"
                        )
                        viewModel.writeReview(review, currentUserId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = (if (isHostReviewingRenter) rating > 0 else (rating > 0 && hostRating > 0)) && uiState !is ReviewUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario)
            ) {
                if (uiState is ReviewUiState.Loading) {
                    CircularProgressIndicator(color = CardSurface, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Invia Recensione", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
