package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.theme.Typography
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(showBackground = true, name = "Test")
fun PersonalInfoCard(
    fullName: String = "Mario Rossi",
    email: String = "mario.rossi@example.com",
    phone: String = "+39 123 456 7890",
    address: String = "Via Roma 1, Milano"
) {
    ProfileSectionCard(
        title = "Personal Information",
        icon = Icons.Default.Person,
        onEditClick = { /* Edit action */ }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoField(label = "LEGAL NAME", value = fullName)
            InfoField(label = "EMAIL", value = email)
            InfoField(label = "PHONE NUMBER", value = phone)
            InfoField(label = "ADDRESS", value = address)
        }
    }
}

@Composable
fun InfoField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
