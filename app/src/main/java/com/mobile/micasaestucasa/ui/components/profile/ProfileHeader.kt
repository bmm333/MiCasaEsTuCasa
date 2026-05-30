package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.components.atomics.AppAvatar
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
fun ProfileHeader(
    name: String = "Mario Rossi",
    memberSince: String = "2024",
    bio: String = "Love traveling and discovering new places!",
    imageUrl: String? = null,
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Container using AppAvatar
        Box(contentAlignment = Alignment.BottomEnd) {
            AppAvatar(
                imageUrl = imageUrl,
                size = 120.dp,
                showBorder = true
            )

            // Red Edit Button
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = 8.dp, y = 8.dp),
                shape = CircleShape,
                color = Primario,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "MEMBER SINCE $memberSince",
            style = Typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Text(
            text = name,
            style = Typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = bio,
            style = Typography.bodyMedium,
            color = Primario,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview() {
    ProfileHeader(
        name = "Alex Malibu",
        bio = "Exploring the best homes around the world."
    )
}
