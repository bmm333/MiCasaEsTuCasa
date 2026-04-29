package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
fun ProfileHeader(
    name: String = "Mario Rossi",
    memberSince: String = "2024",
    bio: String = "Amo viaggiare e scoprire posti nuovi!",
    imageUrl: String? = null,
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Container con bordo e icona placeholder
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Pulsante Edit Rosso
            Surface(
                modifier = Modifier.size(36.dp).offset(x = 8.dp, y = 8.dp),
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
            color = Color(0xFF00A699), // Usando un verde acqua per la bio come da stile moderno
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview() {
    ProfileHeader()
}
