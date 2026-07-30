package com.mobile.micasaestucasa.ui.screens.host

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Cabin
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Villa
import androidx.compose.material3.Button
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.widget.AutocompleteActivity
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.viewmodels.property.CreatePropertyState
import com.mobile.micasaestucasa.ui.viewmodels.property.CreatePropertyViewModel
import com.mobile.micasaestucasa.ui.viewmodels.property.PropertyDraft
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private data class PropertyType(val label: String, val icon: ImageVector)
private val propertyTypes = listOf(
    PropertyType("Appartamento", Icons.Rounded.Apartment),
    PropertyType("Casa", Icons.Rounded.Home),
    PropertyType("Villa", Icons.Rounded.Villa),
    PropertyType("Chalet", Icons.Rounded.Cabin),
    PropertyType("Hotel", Icons.Rounded.Hotel),
    PropertyType("Spiaggia", Icons.Rounded.BeachAccess),
)

@Composable
fun CreatePropertyScreen(
    ownerId: String,
    propertyId: String? = null,
    onNavigateBack: () -> Unit,
    onPublished: () -> Unit,
    viewModel: CreatePropertyViewModel = hiltViewModel()
) {
    val step by viewModel.currentStep.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val isEditMode = propertyId != null

    LaunchedEffect(propertyId) {
        if (propertyId != null) viewModel.loadForEdit(propertyId)
    }

    LaunchedEffect(submitState) {
        if (submitState is CreatePropertyState.Success) {
            viewModel.reset()
            onPublished()
        }
    }

    val progress by animateFloatAsState(
        targetValue = (step + 1f) / viewModel.totalSteps,
        animationSpec = tween(400),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (step == 0) onNavigateBack() else viewModel.prevStep() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color(0xFF222222))
            }
            Text(
                text = if (isEditMode) "Modifica annuncio" else "${step + 1} / ${viewModel.totalSteps}",
                fontSize = 13.sp,
                color = CaptionLabels,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(48.dp))
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(BorderDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .background(Primario)
            )
        }


        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { dir * it } + fadeIn(tween(300))) togetherWith
                    (slideOutHorizontally { -dir * it } + fadeOut(tween(200)))
            },
            label = "step",
            modifier = Modifier.weight(1f)
        ) { currentStep ->
            when (currentStep) {
                0 -> StepPropertyType(draft, viewModel)
                1 -> StepLocation(draft, viewModel)
                2 -> StepCapacity(draft, viewModel)
                3 -> StepAmenities(draft, viewModel)
                4 -> StepPhotos(draft, viewModel)
                5 -> StepTitleDescription(draft, viewModel)
                6 -> StepPrice(draft, viewModel)
                7 -> StepAvailability(draft, viewModel)
                else -> StepPropertyType(draft, viewModel)
            }
        }

        val isLastStep = step == viewModel.totalSteps - 1
        val isNextEnabled = when (step) {
            0 -> draft.propertyType.isNotBlank()
            1 -> draft.city.isNotBlank() && (draft.latitude != 0.0 || draft.longitude != 0.0)
            2 -> draft.capacity >= 1
            3 -> true
            4 -> draft.imageUris.size >= 2
            5 -> draft.title.isNotBlank() && draft.description.isNotBlank()
            6 -> draft.pricePerDay > 0
            7 -> draft.availableFrom.isNotBlank() && draft.availableTo.isNotBlank()
            else -> false
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            if (submitState is CreatePropertyState.Error) {
                Text(
                    text = (submitState as CreatePropertyState.Error).message,
                    color = Color(0xFFE5474B),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = {
                    if (isLastStep) {
                        if (isEditMode) viewModel.saveEdit(ownerId)
                        else viewModel.publish(ownerId)
                    } else viewModel.nextStep()
                },
                enabled = isNextEnabled && submitState !is CreatePropertyState.Submitting,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario)
            ) {
                if (submitState is CreatePropertyState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = CardSurface, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = when {
                            isLastStep && isEditMode -> "Salva modifiche"
                            isLastStep -> "Pubblica annuncio"
                            else -> "Avanti"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepPropertyType(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    StepScaffold(title = "Che tipo di posto hai?", subtitle = "Scegli la categoria che meglio descrive la tua proprietà.") {
        val cols = 2
        val rows = (propertyTypes.size + cols - 1) / cols
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(cols) { col ->
                        val idx = row * cols + col
                        if (idx < propertyTypes.size) {
                            val pt = propertyTypes[idx]
                            val selected = draft.propertyType == pt.label
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) Primario.copy(alpha = 0.08f) else CardSurface)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) Primario else BorderDivider,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { vm.updatePropertyType(pt.label) }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(pt.icon, contentDescription = null, tint = if (selected) Primario else CaptionLabels, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(pt.label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) Primario else Color(0xFF222222), textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepLocation(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    StepScaffold(title = "Dov'è la tua proprietà?", subtitle = "Gli ospiti vedranno solo la città finché non confermano la prenotazione.") {
        val context = LocalContext.current
        val autocompleteLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.let { intent ->
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        vm.updateAddress(place.name ?: "")
                        val cityComponent = place.addressComponents?.asList()?.find { it.types.contains("locality") }
                        val city = cityComponent?.name ?: place.name ?: ""
                        if (city.isNotBlank()) vm.updateCity(city)
                        
                        place.latLng?.let {
                            vm.updateLocation(it.latitude, it.longitude)
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    result.data?.let { intent ->
                        val status = Autocomplete.getStatusFromIntent(intent)
                        android.widget.Toast.makeText(context, "Errore API: ${status.statusMessage}", android.widget.Toast.LENGTH_LONG).show()
                        android.util.Log.e("PlacesError", "Error: ${status.statusMessage}")
                    }
                }
            }
        }

        val launchAutocomplete = {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context)
            autocompleteLauncher.launch(intent)
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = draft.city,
                onValueChange = { vm.updateCity(it) },
                label = { Text("Città") },
                leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = CaptionLabels) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primario, unfocusedBorderColor = BorderDivider, focusedLabelColor = Primario, unfocusedContainerColor = CardSurface, focusedContainerColor = CardSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { launchAutocomplete() })
        }
        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = draft.address,
                onValueChange = { vm.updateAddress(it) },
                label = { Text("Indirizzo") },
                leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = CaptionLabels) },
                placeholder = { Text("Via Roma 10, Milano") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primario, unfocusedBorderColor = BorderDivider, focusedLabelColor = Primario, unfocusedContainerColor = CardSurface, focusedContainerColor = CardSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { launchAutocomplete() })
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Tocca sulla mappa per impostare la posizione esatta",
            fontSize = 14.sp,
            color = CaptionLabels,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        val startPosition = if (draft.latitude != 0.0 || draft.longitude != 0.0) LatLng(draft.latitude, draft.longitude) else LatLng(41.9027835, 12.4963655)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(startPosition, if (draft.latitude != 0.0) 15f else 5f)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, BorderDivider, RoundedCornerShape(14.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    vm.updateLocation(latLng.latitude, latLng.longitude)
                }
            ) {
                if (draft.latitude != 0.0 || draft.longitude != 0.0) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(draft.latitude, draft.longitude)),
                        title = "Posizione selezionata"
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCapacity(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    StepScaffold(title = "Quanti ospiti puoi accogliere?", subtitle = "Puoi modificare questo in qualsiasi momento.") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { vm.updateCapacity(draft.capacity - 1) },
                modifier = Modifier.size(52.dp).clip(CircleShape).background(if (draft.capacity > 1) Primario.copy(0.1f) else BorderDivider)
            ) { Icon(Icons.Rounded.Remove, null, tint = if (draft.capacity > 1) Primario else CaptionLabels) }
            Text(
                text = "${draft.capacity}",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF222222),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            IconButton(
                onClick = { vm.updateCapacity(draft.capacity + 1) },
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Primario.copy(0.1f))
            ) { Icon(Icons.Rounded.Add, null, tint = Primario) }
        }
        Spacer(Modifier.height(8.dp))
        Text("ospiti", fontSize = 16.sp, color = CaptionLabels, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepAmenities(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    val availableKeywords by vm.availableKeywords.collectAsState()
    StepScaffold(title = "Cosa offre il tuo posto?", subtitle = "Seleziona i servizi disponibili. Sono definiti dall'amministratore del sistema.") {
        if (availableKeywords.isEmpty()) {
            Text("Caricamento servizi…", color = CaptionLabels, fontSize = 14.sp)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                availableKeywords.forEach { amenity ->
                    val selected = draft.keywords.contains(amenity)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (selected) Primario else CardSurface)
                            .border(1.dp, if (selected) Primario else BorderDivider, RoundedCornerShape(50.dp))
                            .clickable { vm.toggleKeyword(amenity) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(amenity, fontSize = 14.sp, color = if (selected) Color.White else Color(0xFF222222), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepPhotos(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(input)
                val maxDim = 800
                val scaled = if (bmp.width > maxDim || bmp.height > maxDim) {
                    val ratio = bmp.width.toFloat() / bmp.height
                    val w = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                    val h = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                    Bitmap.createScaledBitmap(bmp, w, h, true)
                } else bmp
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
                val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                vm.addImageUri("data:image/jpeg;base64,$b64")
            } catch (_: Exception) {}
        }
    }

    StepScaffold(title = "Aggiungi le foto", subtitle = "Le foto di qualità aumentano le prenotazioni. Servono almeno 2 foto.") {
        val cols = 2
        val items = draft.imageUris + if (draft.imageUris.size < 5) listOf("ADD") else emptyList()
        val rows = (items.size + cols - 1) / cols
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(cols) { col ->
                        val idx = row * cols + col
                        if (idx < items.size) {
                            if (items[idx] == "ADD") {
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1.3f).clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, BorderDivider, RoundedCornerShape(12.dp))
                                        .clickable { launcher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = CaptionLabels, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("Aggiungi", fontSize = 12.sp, color = CaptionLabels)
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1.3f).clip(RoundedCornerShape(12.dp))) {
                                    AsyncImage(model = items[idx], contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { vm.removeImageUri(items[idx]) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp).clip(CircleShape).background(Color.Black.copy(0.5f))
                                    ) { Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTitleDescription(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    StepScaffold(title = "Dai un nome al tuo posto", subtitle = "Un titolo accattivante aiuta gli ospiti a trovare il tuo annuncio.") {
        OutlinedTextField(
            value = draft.title,
            onValueChange = { if (it.length <= 50) vm.updateTitle(it) },
            label = { Text("Titolo") },
            singleLine = true,
            trailingIcon = { Text("${draft.title.length}/50", fontSize = 11.sp, color = CaptionLabels, modifier = Modifier.padding(end = 8.dp)) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primario, unfocusedBorderColor = BorderDivider, focusedLabelColor = Primario, unfocusedContainerColor = CardSurface, focusedContainerColor = CardSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = draft.description,
            onValueChange = { if (it.length <= 500) vm.updateDescription(it) },
            label = { Text("Descrizione") },
            minLines = 4,
            maxLines = 7,
            trailingIcon = { Text("${draft.description.length}/500", fontSize = 11.sp, color = CaptionLabels, modifier = Modifier.padding(end = 8.dp, bottom = 56.dp)) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primario, unfocusedBorderColor = BorderDivider, focusedLabelColor = Primario, unfocusedContainerColor = CardSurface, focusedContainerColor = CardSurface),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepPrice(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    var priceText by remember { mutableStateOf(if (draft.pricePerDay > 0) draft.pricePerDay.toInt().toString() else "") }
    StepScaffold(title = "Imposta il prezzo per notte", subtitle = "Puoi modificarlo in qualsiasi momento. Riceverai pagamenti in base alle notti prenotate.") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("€", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CaptionLabels)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (priceText.isBlank()) "0" else priceText,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (priceText.isBlank()) BorderDivider else Color(0xFF222222)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("per notte", fontSize = 14.sp, color = CaptionLabels)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = priceText,
                onValueChange = { v ->
                    priceText = v.filter { it.isDigit() }
                    vm.updatePrice(priceText.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Prezzo (€)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primario, unfocusedBorderColor = BorderDivider, focusedLabelColor = Primario, unfocusedContainerColor = CardSurface, focusedContainerColor = CardSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepAvailability(draft: PropertyDraft, vm: CreatePropertyViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateRangeState = rememberDateRangePickerState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ITALIAN) }

    if (showDatePicker) {
        ModalBottomSheet(
            onDismissRequest = {
                val start = dateRangeState.selectedStartDateMillis
                val end = dateRangeState.selectedEndDateMillis
                if (start != null && end != null) {
                    val startDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
                    val endDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
                    vm.updateAvailability(
                        startDate.format(dateFormatter),
                        endDate.format(dateFormatter)
                    )
                }
                showDatePicker = false
            },
            sheetState = sheetState,
            containerColor = CardSurface
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
                        "Seleziona disponibilita",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            )
        }
    }

    val availabilityLabel = remember(draft.availableFrom, draft.availableTo) {
        if (draft.availableFrom.isNotBlank() && draft.availableTo.isNotBlank()) {
            try {
                val from = displayFormatter.format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN).parse(draft.availableFrom)!!
                )
                val to = displayFormatter.format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN).parse(draft.availableTo)!!
                )
                "$from  →  $to"
            } catch (_: Exception) {
                "${draft.availableFrom}  →  ${draft.availableTo}"
            }
        } else {
            null
        }
    }

    StepScaffold(
        title = "Quando e disponibile?",
        subtitle = "Seleziona il periodo in cui la tua casa sara prenotabile."
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .clickable { showDatePicker = true }
                .padding(16.dp)
        ) {
            Text("Periodo di disponibilita", fontSize = 13.sp, color = CaptionLabels)
            Spacer(Modifier.height(8.dp))
            if (availabilityLabel != null) {
                Text(
                    availabilityLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF222222)
                )
            } else {
                Text("Tocca per selezionare le date", fontSize = 15.sp, color = BorderDivider)
            }
        }
    }
}

@Composable
private fun StepScaffold(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        Spacer(Modifier.height(24.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF222222), lineHeight = 30.sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, fontSize = 14.sp, color = CaptionLabels, lineHeight = 20.sp)
        Spacer(Modifier.height(32.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}
