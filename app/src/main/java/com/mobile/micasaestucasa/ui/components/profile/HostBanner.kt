package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
@Preview(showBackground = true, name = "Test")
fun HostBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1EBD0)) // Colore verde chiaro
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Host Your Home",
                style = Typography.titleLarge,
                color = Color(0xFF3D4B2B)
            )
            Text(
                text = "Join our community of exceptional hosts.",
                style = Typography.bodyMedium,
                color = Color(0xFF546341),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = { /* Action */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
