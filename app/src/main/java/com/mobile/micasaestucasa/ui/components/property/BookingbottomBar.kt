package com.mobile.micasaestucasa.ui.components.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Typography

@Composable
@Preview(showBackground = true, name = "Test")
fun BookingBottomBar(
    price: String = "450",
    dates: String = "Jun 12 - 18",
    onBookClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    showChatButton: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "€$price",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " / notte",
                        style = Typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
                Text(
                    text = dates,
                    style = Typography.bodySmall,
                    color = Primario,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }

            if (showChatButton) {
                OutlinedButton(
                    onClick = onChatClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "Messaggio", modifier = Modifier.size(20.dp))
                }
            }

            Button(
                onClick = onBookClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Prenota",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
