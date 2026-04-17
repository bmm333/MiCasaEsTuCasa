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

@Composable
fun PersonalInfoCard(
    fullName: String,
    email: String,
    phone: String,
    address: String
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
