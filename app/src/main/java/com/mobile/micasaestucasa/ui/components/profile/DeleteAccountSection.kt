package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.micasaestucasa.ui.components.atomics.MiCasaPrimaryButton
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.SecondaryText
import com.mobile.micasaestucasa.ui.viewmodels.auth.AuthViewModel

@Composable
fun DeleteAccountSection(
    viewModel: AuthViewModel = hiltViewModel(),
    onAccountDeleted: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthViewModel.AuthUiState.AccountDeleted) onAccountDeleted()
        if (uiState is AuthViewModel.AuthUiState.NeedsReauth) showReauthDialog = true
    }

    // dialog conferma eliminazione
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Elimina account",
                    fontWeight = FontWeight.Bold,
                    color = ErrorColor
                )
            },
            text = {
                Column {
                    Text(
                        "Questa azione è permanente e irreversibile:",
                        fontWeight = FontWeight.SemiBold,
                        color = HeadingText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Il tuo profilo sarà eliminato",
                        "Tutte le tue proprietà saranno rimosse",
                        "Le prenotazioni attive saranno cancellate",
                        "Le tue conversazioni saranno eliminate"
                    ).forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                null,
                                tint = ErrorColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item, fontSize = 13.sp, color = SecondaryText)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sì, elimina account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Annulla", color = CaptionLabels)
                }
            }
        )
    }

    // dialog re-authentication
    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = { showReauthDialog = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Conferma identità",
                    fontWeight = FontWeight.Bold,
                    color = HeadingText
                )
            },
            text = {
                Column {
                    Text(
                        "Per eliminare l'account inserisci la tua password.",
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = { reauthPassword = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) {
                                        Icons.Rounded.Visibility
                                    } else {
                                        Icons.Rounded.VisibilityOff
                                    },
                                    null,
                                    tint = CaptionLabels
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primario,
                            cursorColor = Primario
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                MiCasaPrimaryButton(
                    text = "Conferma ed elimina",
                    enabled = uiState !is AuthViewModel.AuthUiState.Loading,
                    onClick = {
                        viewModel.reauthenticateAndDelete(reauthPassword)
                        showReauthDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = { showReauthDialog = false }) {
                    Text("Annulla", color = CaptionLabels)
                }
            }
        )
    }

    // bottone nella UI settings
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showConfirmDialog = true }
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ErrorColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.DeleteForever,
                null,
                tint = ErrorColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                "Elimina account",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = ErrorColor
            )
            Text(
                "Rimuove tutti i tuoi dati permanentemente",
                fontSize = 12.sp,
                color = CaptionLabels
            )
        }
    }
}
