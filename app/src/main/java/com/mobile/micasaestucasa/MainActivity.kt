package com.mobile.micasaestucasa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile.micasaestucasa.ui.components.atomics.MiCasaPrimaryButton
import com.mobile.micasaestucasa.ui.navigation.AppNavigation
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.Caution
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.theme.SecondaryText
import com.mobile.micasaestucasa.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private var pendingNotificationType by mutableStateOf<String?>(null)
    private var pendingNotificationTargetId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        readNotificationExtras(intent)
        setContent {
            MiCasaEsTuCasaTheme {
                val sessionEvent by mainViewModel.sessionEvent.collectAsStateWithLifecycle()
                var showBanDialog by rememberSessionDialogState(false)
                var showSuspendDialog by rememberSessionDialogState(false)

                LaunchedEffect(sessionEvent) {
                    when (sessionEvent) {
                        is MainViewModel.SessionEvent.Banned -> showBanDialog = true
                        is MainViewModel.SessionEvent.Suspended -> showSuspendDialog = true
                        else -> {}
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        pendingNotificationType = pendingNotificationType,
                        pendingNotificationTargetId = pendingNotificationTargetId,
                        onNotificationHandled = {
                            pendingNotificationType = null
                            pendingNotificationTargetId = null
                        }
                    )
                }

                if (showBanDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        containerColor = CardSurface,
                        shape = RoundedCornerShape(20.dp),
                        title = {
                            Text(
                                "Account sospeso",
                                fontWeight = FontWeight.Bold,
                                color = ErrorColor
                            )
                        },
                        text = {
                            Text(
                                "Il tuo account è stato bannato per violazione dei termini di servizio. " +
                                    "Non puoi più accedere a MiCasa.",
                                color = SecondaryText
                            )
                        },
                        confirmButton = {
                            MiCasaPrimaryButton(
                                text = "Chiudi app",
                                onClick = { finish() }
                            )
                        }
                    )
                }

                if (showSuspendDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        containerColor = CardSurface,
                        shape = RoundedCornerShape(20.dp),
                        title = {
                            Text(
                                "Account temporaneamente sospeso",
                                fontWeight = FontWeight.Bold,
                                color = Caution
                            )
                        },
                        text = {
                            Text(
                                "Il tuo account è stato sospeso temporaneamente. " +
                                    "Contatta il supporto per maggiori informazioni.",
                                color = SecondaryText
                            )
                        },
                        confirmButton = {
                            MiCasaPrimaryButton(
                                text = "Chiudi app",
                                onClick = { finish() }
                            )
                        }
                    )
                }
            }
        }
        Log.d("ARCH_TEST", "Architecture pipeline OK")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readNotificationExtras(intent)
    }

    private fun readNotificationExtras(intent: Intent?) {
        pendingNotificationType = intent?.getStringExtra("notificationType")
        pendingNotificationTargetId = intent?.getStringExtra("targetID")
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}

@Composable
private fun rememberSessionDialogState(initial: Boolean) =
    androidx.compose.runtime.remember { mutableStateOf(initial) }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MiCasaEsTuCasaTheme {
        Greeting("Android")
    }
}
