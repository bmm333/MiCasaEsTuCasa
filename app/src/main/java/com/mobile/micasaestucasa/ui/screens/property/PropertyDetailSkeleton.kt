package com.mobile.micasaestucasa.ui.screens.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobile.micasaestucasa.ui.components.atomics.ShimmerEffect

@Composable
fun PropertyDetailSkeleton(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            ShimmerEffect(modifier = Modifier.fillMaxWidth(0.7f).height(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
            Spacer(modifier = Modifier.height(24.dp))
            ShimmerEffect(modifier = Modifier.fillMaxWidth().height(80.dp))
        }
    }
}
