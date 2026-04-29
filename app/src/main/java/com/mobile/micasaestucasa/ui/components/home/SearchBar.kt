package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.components.atomics.GradientButton
import com.mobile.micasaestucasa.ui.components.atomics.SearchInput

@Composable
fun SearchBar(
    location: String = "",
    onLocationChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    var dates by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchInput(
                value = location,
                onValueChange = onLocationChange,
                placeholder = "Dove vai?",
                leadingIcon = Icons.Default.LocationOn,
                modifier = Modifier.testTag("search_input_location")
            )

            SearchInput(
                value = dates,
                onValueChange = { dates = it },
                placeholder = "Aggiungi date",
                leadingIcon = Icons.Default.CalendarToday
            )

            GradientButton(
                text = "Cerca",
                onClick = onSearchClick,
                modifier = Modifier.testTag("search_button")
            )
        }
    }
}

@Preview(showBackground = true, name = "SearchBar Preview")
@Composable
fun SearchBarPreview() {
    SearchBar()
}
