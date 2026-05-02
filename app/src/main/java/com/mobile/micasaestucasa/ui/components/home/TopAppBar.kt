package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.components.atomics.AppAvatar
import com.mobile.micasaestucasa.ui.theme.IconSize
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Topnavigation(
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
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
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                AppAvatar(
                    imageRes = android.R.drawable.ic_menu_gallery, // Default value, will be replaced by user profile image
                    size = IconSize.Medium
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Primario
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Preview(showBackground = true, name = "Topnavigation Preview")
@Composable
fun TopnavigationPreview() {
    Topnavigation()
}
