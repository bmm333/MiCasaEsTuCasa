package com.mobile.micasaestucasa.ui.screens.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Primario

@Composable
fun HostIntroScreen(
    onNavigateBack: () -> Unit,
    onGetStarted: () -> Unit,
    onSkip: () -> Unit = onNavigateBack,
    canSkip: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Top gradient accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Brush.horizontalGradient(listOf(Primario, Color(0xFFFC642D))))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canSkip) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro", tint = Color(0xFF222222))
                    }
                } else {
                    // Mandatory onboarding: no back arrow
                    Spacer(Modifier.height(48.dp))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Primario.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Home, null, tint = Primario, modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Diventa host in tre\nsemplici passi.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF222222),
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "MiCasaEsTuCasa ti guida in tutto il processo. Ci vogliono circa 10 minuti.",
                    fontSize = 15.sp,
                    color = Color(0xFF767676),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(40.dp))

                HostStep(
                    1,
                    Icons.Rounded.Home,
                    "Descrivi il tuo posto",
                    "Categoria, location, indirizzo e quanti ospiti puoi accogliere.",
                    Primario
                )
                HostStep(
                    2,
                    Icons.Rounded.AddPhotoAlternate,
                    "Aggiungi foto e titolo",
                    "Foto di qualità e una descrizione che cattura l'essenza del luogo.",
                    Color(0xFFFC642D)
                )
                HostStep(
                    3,
                    Icons.Rounded.CurrencyExchange,
                    "Imposta il prezzo",
                    "Prezzo per notte e date di disponibilità. Modificabili in ogni momento.",
                    Color(0xFF00A699)
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFE5474B).copy(alpha = 0.07f)).padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.People, null, tint = Primario, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Guadagna ospitando", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF222222))
                            Text("Migliaia di ospiti cercano posti unici come il tuo.", fontSize = 13.sp, color = Color(0xFF767676), lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primario)
                ) {
                    Text("Inizia", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White)
                }

                Spacer(Modifier.height(12.dp))

                if (canSkip) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Non ora",
                            fontSize = 15.sp,
                            color = Color(0xFF767676),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun HostStep(number: Int, icon: ImageVector, title: String, description: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$number.", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = accentColor)
                Spacer(Modifier.width(4.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
            }
            Spacer(Modifier.height(4.dp))
            Text(description, fontSize = 14.sp, color = Color(0xFF767676), lineHeight = 20.sp)
        }
    }
}
