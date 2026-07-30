package com.mobile.micasaestucasa.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.domain.model.search.SearchResult
import com.mobile.micasaestucasa.domain.model.search.SearchSortOrder
import com.mobile.micasaestucasa.ui.theme.Accenti
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.Typography
import com.mobile.micasaestucasa.ui.viewmodels.search.SearchUiState
import com.mobile.micasaestucasa.ui.viewmodels.search.SearchViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// ── Amenity categories ──────────────────────────────────────────────────
private data class AmenityCategory(val label: String, val keyword: String, val icon: ImageVector)

private val amenityCategories = listOf(
    AmenityCategory("Piscina", "piscina", Icons.Default.Pool),
    AmenityCategory("Spiaggia", "spiaggia", Icons.Default.BeachAccess),
    AmenityCategory("WiFi", "wifi", Icons.Default.Wifi),
    AmenityCategory("Montagna", "montagna", Icons.Default.Park),
    AmenityCategory("Lago", "lago", Icons.Default.Waves),
    AmenityCategory("Camino", "camino", Icons.Default.Fireplace),
    AmenityCategory("Vista", "vista", Icons.Default.HolidayVillage),
    AmenityCategory("Aria Cond.", "aria condizionata", Icons.Default.Air),
    AmenityCategory("Cabin", "cabin", Icons.Default.Cabin)
)

private val sortOrderLabels = mapOf(
    SearchSortOrder.RELEVANCE to "Rilevanza",
    SearchSortOrder.PRICE_ASC to "Prezzo: basso → alto",
    SearchSortOrder.PRICE_DESC to "Prezzo: alto → basso",
    SearchSortOrder.RATING_DESC to "Valutazione"
)

// ═══════════════════════════════════════════════════════════════════════
//  SearchScreen – entry point
// ═══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProperty: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ── Form state ──────────────────────────────────────────────────
    var city by remember { mutableStateOf("") }
    var guestsCount by remember { mutableIntStateOf(1) }
    var checkIn by remember { mutableStateOf("") }
    var checkOut by remember { mutableStateOf("") }
    var maxPrice by remember { mutableFloatStateOf(1000f) }
    var priceFilterEnabled by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(SearchSortOrder.RELEVANCE) }
    val selectedCategories = remember { mutableStateListOf<String>() }

    // ── Sheet / dropdown state ───────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = remember { SimpleDateFormat("dd MMM", Locale.ITALIAN) }

    // ── Date Range Picker state ──────────────────────────────────────
    val dateRangeState = rememberDateRangePickerState()

    // Show date picker sheet
    if (showDatePicker) {
        ModalBottomSheet(
            onDismissRequest = {
                showDatePicker = false
                val start = dateRangeState.selectedStartDateMillis
                val end = dateRangeState.selectedEndDateMillis
                if (start != null && end != null) {
                    val startDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
                    val endDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
                    checkIn = startDate.format(dateFormatter)
                    checkOut = endDate.format(dateFormatter)
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DateRangePicker(
                state = dateRangeState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Primario,
                    todayDateBorderColor = Primario,
                    dayInSelectionRangeContainerColor = Primario.copy(alpha = 0.15f)
                ),
                title = {
                    Text(
                        "Seleziona date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                headline = {
                    val start = dateRangeState.selectedStartDateMillis
                    val end = dateRangeState.selectedEndDateMillis
                    val headline = when {
                        start == null -> "Seleziona data di arrivo"
                        end == null -> "Seleziona data di partenza"
                        else -> {
                            val s = displayFormatter.format(Date(start))
                            val e = displayFormatter.format(Date(end))
                            "$s  →  $e"
                        }
                    }
                    Text(
                        headline,
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                        style = Typography.bodyLarge,
                        color = Primario,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val start = dateRangeState.selectedStartDateMillis
                    val end = dateRangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        val startDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
                        val endDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
                        checkIn = startDate.format(dateFormatter)
                        checkOut = endDate.format(dateFormatter)
                    }
                    showDatePicker = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Conferma date", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    Scaffold(
        containerColor = ScreenBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Top bar ─────────────────────────────────────────────
            item {
                SearchTopBar(
                    onBack = {
                        viewModel.reset()
                        onNavigateBack()
                    }
                )
            }

            // ── City input ──────────────────────────────────────────
            item {
                SearchSectionTitle("Destinazione")
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = { Text("Es. Roma, Milano, Firenze…", color = CaptionLabels) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primario)
                    },
                    trailingIcon = if (city.isNotEmpty()) {
                        {
                            IconButton(onClick = { city = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancella", tint = CaptionLabels)
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // ── Date picker ─────────────────────────────────────────
            item {
                SearchSectionTitle("Date del soggiorno")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DatePickerButton(
                        label = "Arrivo",
                        date = checkIn.ifEmpty { null }?.let {
                            runCatching {
                                LocalDate.parse(it).let { d ->
                                    displayFormatter.format(
                                        Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())
                                    )
                                }
                            }.getOrNull()
                        },
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true }
                    )
                    DatePickerButton(
                        label = "Partenza",
                        date = checkOut.ifEmpty { null }?.let {
                            runCatching {
                                LocalDate.parse(it).let { d ->
                                    displayFormatter.format(
                                        Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())
                                    )
                                }
                            }.getOrNull()
                        },
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true }
                    )
                }
            }

            // ── Guests counter ──────────────────────────────────────
            item {
                SearchSectionTitle("Ospiti")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Numero ospiti", style = Typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { if (guestsCount > 1) guestsCount-- },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (guestsCount > 1) Primario.copy(alpha = 0.1f) else Color(0xFFF0F0F0), CircleShape)
                    ) {
                        Text("−", fontSize = 18.sp, color = if (guestsCount > 1) Primario else CaptionLabels, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "$guestsCount",
                        modifier = Modifier.padding(horizontal = 14.dp),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { if (guestsCount < 20) guestsCount++ },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primario.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Text("+", fontSize = 18.sp, color = Primario, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Amenity / Category chips ─────────────────────────────
            item {
                SearchSectionTitle("Servizi e caratteristiche")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(amenityCategories) { cat ->
                        val isSelected = selectedCategories.contains(cat.keyword)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedCategories.remove(cat.keyword)
                                } else {
                                    selectedCategories.add(cat.keyword)
                                }
                            },
                            label = { Text(cat.label, style = Typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primario.copy(alpha = 0.12f),
                                selectedLabelColor = Primario,
                                selectedLeadingIconColor = Primario
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Primario,
                                borderColor = Color(0xFFDDDDDD)
                            )
                        )
                    }
                }
            }

            // ── Price slider ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Prezzo max / notte",
                        style = Typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = priceFilterEnabled,
                        onClick = { priceFilterEnabled = !priceFilterEnabled },
                        label = {
                            Text(
                                if (priceFilterEnabled) "€${maxPrice.toInt()}" else "Nessun limite",
                                style = Typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primario.copy(alpha = 0.12f),
                            selectedLabelColor = Primario
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = priceFilterEnabled,
                            selectedBorderColor = Primario,
                            borderColor = Color(0xFFDDDDDD)
                        )
                    )
                }
                AnimatedVisibility(visible = priceFilterEnabled) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Slider(
                            value = maxPrice,
                            onValueChange = { maxPrice = it },
                            valueRange = 50f..2000f,
                            steps = 38,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Primario,
                                activeTrackColor = Primario,
                                inactiveTrackColor = Primario.copy(alpha = 0.2f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("€50", style = Typography.labelSmall, color = CaptionLabels)
                            Text("€${maxPrice.toInt()}/notte", style = Typography.labelMedium, color = Primario, fontWeight = FontWeight.Bold)
                            Text("€2000", style = Typography.labelSmall, color = CaptionLabels)
                        }
                    }
                }
            }

            // ── Sort order ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ordina per",
                        style = Typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                            modifier = Modifier.clickable { showSortDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = Primario, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    sortOrderLabels[selectedSort] ?: "Rilevanza",
                                    style = Typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CaptionLabels, modifier = Modifier.size(14.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            sortOrderLabels.forEach { (order, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            fontWeight = if (order == selectedSort) FontWeight.Bold else FontWeight.Normal,
                                            color = if (order == selectedSort) Primario else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedSort = order
                                        showSortDropdown = false
                                        if (uiState is SearchUiState.Results) {
                                            viewModel.applySortOrder(order)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Divider ──────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEEEEEE)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Search button ────────────────────────────────────────
            item {
                Button(
                    onClick = {
                        if (city.isNotBlank() && checkIn.isNotBlank() && checkOut.isNotBlank()) {
                            viewModel.search(
                                city = city,
                                startDate = checkIn,
                                endDate = checkOut,
                                guestsCount = guestsCount,
                                keywords = selectedCategories.toList(),
                                maxPricePerDay = if (priceFilterEnabled) maxPrice.toDouble() else null
                            )
                        }
                    },
                    enabled = city.isNotBlank() && checkIn.isNotBlank() && checkOut.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primario,
                        disabledContainerColor = Primario.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cerca proprietà",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Results section ──────────────────────────────────────
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Primario, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Ricerca in corso…", color = CaptionLabels, style = Typography.bodyMedium)
                            }
                        }
                    }
                }

                is SearchUiState.Results -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${state.results.size} proprietà trovate",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedCategories.isNotEmpty()) {
                                Surface(
                                    color = Secondary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        "${selectedCategories.size} filtri attivi",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = Typography.labelSmall,
                                        color = Secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    items(state.results) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onNavigateToProperty(result.property.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                is SearchUiState.Empty -> {
                    item {
                        EmptySearchState(
                            onReset = {
                                selectedCategories.clear()
                                priceFilterEnabled = false
                                viewModel.search(city, checkIn, checkOut, guestsCount)
                            }
                        )
                    }
                }

                is SearchUiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    state.message,
                                    color = ErrorColor,
                                    style = Typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.reset() }) {
                                    Text("Riprova", color = Primario)
                                }
                            }
                        }
                    }
                }

                is SearchUiState.Idle -> {
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                    item {
                        IdleSearchHint()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Sub-components
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SearchTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Torna indietro",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            "Cerca proprietà",
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun SearchSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        style = Typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DatePickerButton(
    label: String,
    date: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (date != null) Primario else Color(0xFFDDDDDD)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (date != null) Primario else CaptionLabels,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = Typography.labelSmall, color = CaptionLabels)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                date ?: "Seleziona",
                style = Typography.bodyMedium,
                fontWeight = if (date != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (date != null) MaterialTheme.colorScheme.onSurface else CaptionLabels
            )
        }
    }
}

@Composable
private fun IdleSearchHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Primario.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Inserisci una destinazione,\nle date e il numero di ospiti",
            style = Typography.bodyLarge,
            color = CaptionLabels,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Usa i filtri per trovare la casa perfetta!",
            style = Typography.bodyMedium,
            color = CaptionLabels.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmptySearchState(onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = null,
            tint = CaptionLabels.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Nessuna proprietà trovata",
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Prova a modificare i filtri o a cercare\nin un'altra destinazione.",
            style = Typography.bodyMedium,
            color = CaptionLabels
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onReset) {
            Text("Rimuovi filtri", color = Primario, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SearchResultCard
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val property = result.property
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = property.imageUrls.firstOrNull()
                        ?: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                )
                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )
                // Rating badge
                if (property.rating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Accenti,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "%.1f".format(property.rating),
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Nights badge at bottom-left
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Primario,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${result.nights} notti",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    property.title,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CaptionLabels,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        property.city,
                        style = Typography.bodySmall,
                        color = CaptionLabels,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Totale soggiorno",
                            style = Typography.labelSmall,
                            color = CaptionLabels
                        )
                        Text(
                            "€%.0f".format(result.totalPrice),
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primario
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "€%.0f / notte".format(property.pricePerDay),
                            style = Typography.bodySmall,
                            color = CaptionLabels
                        )
                        if (property.reviewsCount > 0) {
                            Text(
                                "${property.reviewsCount} recensioni",
                                style = Typography.labelSmall,
                                color = CaptionLabels.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
