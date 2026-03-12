package com.readingledger.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.ui.theme.*

@Composable
fun AnalyticsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(Bg)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Analytics", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Text(
            "Complete at least 1 book\nto see analytics.",
            color = TextMedium, fontSize = 16.sp,
            fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // TODO: Stats grid, category bars, rating distribution, pace charts
    }
}
