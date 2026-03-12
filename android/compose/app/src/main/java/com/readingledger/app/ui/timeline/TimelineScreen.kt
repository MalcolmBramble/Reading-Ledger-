package com.readingledger.app.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.ui.theme.*

@Composable
fun TimelineScreen(onBookClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Bg)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Timeline", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Text(
            "No activity yet.\nStart reading to see your timeline.",
            color = TextDim, fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // TODO: Build timeline event list from DataStore
    }
}
