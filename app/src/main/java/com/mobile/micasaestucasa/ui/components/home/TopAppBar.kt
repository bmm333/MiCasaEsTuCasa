package com.mobile.micasaestucasa.ui.components.home

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, name = "Test")
fun Topnavigation() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "MiCasaEsTuCasa",
                style = Typography.titleLarge,
                color = Primario,
                fontWeight = FontWeight.ExtraBold
            )
        },
        navigationIcon = {
            Row(modifier = Modifier.padding(start = 16.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_menu_gallery), // Placeholder
                    contentDescription = "Profilo Utente",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Azione notifiche */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifiche",
                    tint = Primario
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
