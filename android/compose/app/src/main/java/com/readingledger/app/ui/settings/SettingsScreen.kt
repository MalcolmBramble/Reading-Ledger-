package com.readingledger.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Bg)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back", color = Accent) }
        Text("Settings", color = TextPrimary, fontSize = 22.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        // TODO: Annual goal, category goals, challenges, import/export, reset, clear
        Text("Settings content coming soon.", color = TextDim, fontSize = 14.sp)
    }
}
