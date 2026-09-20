package com.mobile.micasaestucasa.ui.components.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mobile.micasaestucasa.R
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    text: String = "Continua con Google",
    onSignInSuccess: (idToken: String) -> Unit,
    onSignInFailure: (Exception) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }

    val webClientId = stringResource(R.string.web_client_id)

    Button(
        onClick = {
            if (isSigningIn || isLoading) return@Button
            isSigningIn = true

            coroutineScope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)

                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )

                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            onSignInSuccess(googleIdTokenCredential.idToken)
                        } catch (e: Exception) {
                            onSignInFailure(Exception("Failed to parse Google ID Token: ${e.message}"))
                        }
                    } else {
                        onSignInFailure(Exception("Unexpected credential type: ${credential.type}"))
                    }
                } catch (e: GetCredentialCancellationException) {
                    // User cancelled, ignore
                } catch (e: Exception) {
                    onSignInFailure(e)
                } finally {
                    isSigningIn = false
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CardSurface,
            contentColor = HeadingText
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading || isSigningIn) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Primario,
                strokeWidth = 2.dp
            )
        } else {
            // Placeholder text for Google logo
            Text(
                text = "G",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color(0xFFDB4437) // Google Red
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}
