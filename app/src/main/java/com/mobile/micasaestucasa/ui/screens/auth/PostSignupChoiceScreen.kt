package com.mobile.micasaestucasa.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario

@Composable
fun PostSignupChoiceScreen(
    onChooseRent: () -> Unit,
    onChooseHost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Come vuoi usare MiCasa?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = HeadingText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Puoi sempre cambiare idea e diventare ospite in seguito.",
            fontSize = 15.sp,
            color = CaptionLabels,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onChooseRent,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primario)
        ) {
            Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Voglio affittare", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onChooseHost,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.Key, contentDescription = null, tint = Primario)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Voglio ospitare", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primario)
        }
    }
}
