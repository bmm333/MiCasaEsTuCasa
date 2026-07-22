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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobile.micasaestucasa.ui.navigation.AppNavigation
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.libraries.places.api.Places

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingNotificationType by mutableStateOf<String?>(null)
    private var pendingNotificationTargetId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Places initialization moved to Application class
        requestNotificationPermission()
        readNotificationExtras(intent)
        setContent {
            MiCasaEsTuCasaTheme {
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
