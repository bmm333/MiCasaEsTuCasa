package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.components.atomics.GradientButton
import com.mobile.micasaestucasa.ui.components.atomics.SearchInput
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
fun JournalSection(
    onJoinClick: (String) -> Unit = {},
    onReadMoreClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "THE JOURNAL",
                style = Typography.labelSmall,
                color = Primario,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Curated living, delivered to your inbox.",
                style = Typography.titleLarge,
                lineHeight = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Discover hidden gems, interior design inspiration, and exclusive offers for your next escape.",
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            SearchInput(
                value = "",
                onValueChange = {},
                placeholder = "Email address"
            )

            Spacer(modifier = Modifier.height(16.dp))

            GradientButton(
                text = "Join Us",
                onClick = { onJoinClick("") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brand-consistent interior image
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // To be replaced with brand asset
                contentDescription = "Interior design inspiration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .rotate(2f)
                    .scale(1.1f)
            )
        }
    }
}

@Preview(showBackground = true, name = "JournalSection Preview")
@Composable
fun JournalSectionPreview() {
    JournalSection()
}
