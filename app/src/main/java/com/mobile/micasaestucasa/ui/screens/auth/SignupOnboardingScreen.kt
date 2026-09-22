package com.mobile.micasaestucasa.ui.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.components.atomics.AppAvatar
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupOnboardingScreen(
    userViewModel: UserViewModel = hiltViewModel(),
    onCompleted: () -> Unit
) {
    val userState by userViewModel.userState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }

    var firstName by remember {
        mutableStateOf(
            firebaseUser?.email?.substringBefore("@")?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            } ?: ""
        )
    }
    var lastName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                profileImageUrl = uriToBase64DataUri(context.contentResolver.openInputStream(it))
            } catch (_: Exception) {
                errorMessage = "Errore nel caricamento della foto"
            }
        }
    }

    LaunchedEffect(userState) {
        if (isSaving && userState is Resource.Success) {
            isSaving = false
            onCompleted()
        } else if (isSaving && userState is Resource.Error) {
            isSaving = false
            errorMessage = (userState as Resource.Error).message
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Completa il tuo profilo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = HeadingText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    ) { paddingValues ->
        when (userState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primario)
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (userState as Resource.Error).message,
                            color = ErrorColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { userViewModel.loadUser() }) {
                            Text("Riprova")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val currentUser = (userState as Resource.Success<User?>).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Dicci chi sei per iniziare",
                        fontSize = 14.sp,
                        color = CaptionLabels
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        AppAvatar(
                            imageUrl = profileImageUrl,
                            size = 100.dp,
                            showBorder = true,
                            placeholderRes = android.R.drawable.ic_menu_gallery
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SCEGLI FOTO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tocca per scegliere dalla galleria",
                        fontSize = 12.sp,
                        color = CaptionLabels
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OnboardingTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "Nome",
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OnboardingTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Cognome",
                        imeAction = ImeAction.Done
                    )

                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (errorMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = ErrorColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage ?: "", color = ErrorColor, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            if (firstName.isBlank()) {
                                errorMessage = "Il nome è obbligatorio"
                                return@Button
                            }
                            if (lastName.isBlank()) {
                                errorMessage = "Il cognome è obbligatorio"
                                return@Button
                            }
                            val uid = firebaseUser?.uid
                            if (uid == null) {
                                errorMessage = "Sessione scaduta: effettua nuovamente il login"
                                return@Button
                            }

                            isSaving = true
                            val updatedUser = currentUser?.copy(
                                name = firstName.trim(),
                                lastName = lastName.trim(),
                                profileCompleted = true,
                                profileImageUrl = profileImageUrl
                            ) ?: User(
                                id = uid,
                                name = firstName.trim(),
                                lastName = lastName.trim(),
                                email = firebaseUser.email ?: "",
                                roles = listOf(UserRole.GUEST),
                                profileCompleted = true,
                                profileImageUrl = profileImageUrl
                            )
                            userViewModel.updateProfile(updatedUser)
                        },
                        enabled = !isSaving && firstName.isNotBlank() && lastName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = CardSurface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Continua", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = CaptionLabels)
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primario,
            unfocusedBorderColor = BorderDivider,
            focusedLabelColor = Primario,
            unfocusedContainerColor = CardSurface,
            focusedContainerColor = CardSurface
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = imeAction),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun uriToBase64DataUri(inputStream: java.io.InputStream?): String {
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val maxDimension = 300
    val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val dstWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
        val dstHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
        Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true)
    } else {
        bitmap
    }
    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64String"
}
