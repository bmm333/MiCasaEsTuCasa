package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(showBackground = true, name = "Test")
fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MiCasaEsTuCasa",
            color = Primario,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "© 2024 MiCasaEsTuCasa. A Warmly Curated Experience.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = Typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            val footerTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            TextButton(onClick = { /* Azione */ }) { Text("The Journal", color = footerTextColor) }
            TextButton(onClick = { /* Azione */ }) { Text("Our Story", color = footerTextColor) }
            TextButton(onClick = { /* Azione */ }) { Text("Host a Home", color = footerTextColor) }
        }
        TextButton(onClick = { /* Azione */ }) { Text("Concierge", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
