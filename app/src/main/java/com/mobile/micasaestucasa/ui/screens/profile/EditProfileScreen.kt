package com.mobile.micasaestucasa.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.theme.BorderDivider
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.ScreenBackground
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    isNewUser: Boolean,
    userViewModel: UserViewModel = hiltViewModel(),
    onProfileSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val userState by userViewModel.userState.collectAsStateWithLifecycle()

    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val firebaseUser = remember { auth.currentUser }

    var name by remember { mutableStateOf(firebaseUser?.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "") }
    var email by remember { mutableStateOf(firebaseUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Load user data when available and fill the fields
    LaunchedEffect(userState) {
        if (userState is Resource.Success && !isInitialized) {
            val user = (userState as Resource.Success<User?>).data
            if (user != null) {
                name = user.name
                email = user.email
                phone = user.phone
                address = user.address
                bio = user.bio
                isInitialized = true
            }
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewUser) "Completa il tuo profilo" else "Modifica profilo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = HeadingText
                    )
                },
                navigationIcon = {
                    if (!isNewUser) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Indietro",
                                tint = HeadingText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface
                )
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
                            color = ErrorColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { userViewModel.loadUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primario),
                            shape = RoundedCornerShape(14.dp)
                        ) {
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

                    // Avatar section
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Primario),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = CardSurface,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isNewUser) {
                        Text(
                            text = "Dicci qualcosa di te per iniziare",
                            fontSize = 14.sp,
                            color = CaptionLabels
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // — Name field —
                    ProfileTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome completo",
                        icon = Icons.Rounded.Person,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // — Email field (read-only) —
                    ProfileTextField(
                        value = email,
                        onValueChange = {},
                        label = "Email",
                        icon = Icons.Rounded.Email,
                        readOnly = true,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // — Phone field —
                    ProfileTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Telefono",
                        icon = Icons.Rounded.Phone,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // — Address field —
                    ProfileTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Indirizzo",
                        icon = Icons.Rounded.LocationOn,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // — Bio field (multiline) —
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = CaptionLabels
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = BorderDivider,
                            focusedLabelColor = Primario,
                            unfocusedContainerColor = CardSurface,
                            focusedContainerColor = CardSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error message
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
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Save button
                    Button(
                        onClick = {
                            errorMessage = null
                            if (name.isBlank()) {
                                errorMessage = "Il nome è obbligatorio"
                                return@Button
                            }

                            val uid = firebaseUser?.uid
                            if (uid == null) {
                                errorMessage = "Sessione scaduta: effettua nuovamente il login"
                                return@Button
                            }

                            isSaving = true
                            val updatedUser = currentUser?.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                bio = bio.trim()
                            ) ?: User(
                                id = uid,
                                name = name.trim(),
                                email = email.trim(),
                                roles = listOf(UserRole.GUEST),
                                phone = phone.trim(),
                                address = address.trim(),
                                bio = bio.trim()
                            )
                            userViewModel.updateProfile(updatedUser)
                        },
                        enabled = !isSaving && name.isNotBlank(),
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
                            Text(
                                text = if (isNewUser) "Inizia" else "Salva modifiche",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Observe save result
                LaunchedEffect(userState) {
                    if (isSaving && userState is Resource.Success) {
                        isSaving = false
                        onProfileSaved()
                    } else if (isSaving && userState is Resource.Error) {
                        isSaving = false
                        errorMessage = (userState as Resource.Error).message
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CaptionLabels
            )
        },
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primario,
            unfocusedBorderColor = BorderDivider,
            focusedLabelColor = Primario,
            unfocusedContainerColor = if (readOnly) Color(0xFFF0F0F0) else CardSurface,
            focusedContainerColor = if (readOnly) Color(0xFFF0F0F0) else CardSurface,
            disabledBorderColor = BorderDivider,
            disabledLabelColor = CaptionLabels,
            disabledTextColor = CaptionLabels
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
