package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.components.atomics.GradientButton
import com.mobile.micasaestucasa.ui.components.atomics.SearchInput

@Composable
fun SearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onSearchClick: (String) -> Unit = {}
) {
    var dates by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchInput(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Where are you going?",
                leadingIcon = Icons.Default.LocationOn,
                modifier = Modifier.testTag("search_input_location")
            )

            SearchInput(
                value = dates,
                onValueChange = { dates = it },
                placeholder = "Add dates",
                leadingIcon = Icons.Default.CalendarToday
            )

            GradientButton(
                text = "Search",
                onClick = { onSearchClick(query) },
                modifier = Modifier.testTag("search_button")
            )
        }
    }
}

@Preview(showBackground = true, name = "SearchBar Preview")
@Composable
fun SearchBarPreview() {
    SearchBar(query = "Malibu")
}
