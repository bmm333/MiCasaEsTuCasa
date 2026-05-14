package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyUiState
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun PropertyDetailScree(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(propertyId) {
        viewModel.loadPropertyNyId(propertyId)
    }
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(CardSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = HeadingText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        bottomBar = {
            when (val state = uiState) {
                is PropertyUiState.DetailSuccess -> {
                    PropertyBottomBar(
                        property = state.property,
                        onBook = { onNavigateToBooking(propertyId) },
                        onChat = { onNavigateToChat(state.property.ownerId) }
                    )
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PropertyUiState.Loading -> {
                PropertyDetailSkeleton(paddingValues)
            }
            is PropertyUiState.DetailSuccess -> {
                PropertyDetailContent(
                    property = state.property,
                    paddingValues = paddingValues
                )
            }
            is PropertyUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = ErrorColor)
                }
            }
            else -> {}
        }
    }
}
