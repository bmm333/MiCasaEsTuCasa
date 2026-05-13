package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Sfumatura

@Composable
fun KeywordChip(keyword: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Sfumatura
    ) {
        Text(
            text = keyword,
            fontSize = 13.sp,
            color = Primario,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
