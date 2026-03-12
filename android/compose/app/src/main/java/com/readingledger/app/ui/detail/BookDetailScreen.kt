package com.readingledger.app.ui.detail

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
fun BookDetailScreen(bookId: String, onBack: () -> Unit, onEdit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Bg)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        // TODO: Load book from DataStore by bookId
        TextButton(onClick = onBack) { Text("← Back", color = Accent) }
        Spacer(Modifier.height(12.dp))
        Text("Book Detail", color = TextPrimary, fontSize = 24.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Text("Book ID: $bookId", color = TextDim, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        // TODO: Rating stars, progress bar, notes, quotes, sessions, timer
        TextButton(onClick = onEdit) { Text("Edit", color = Accent) }
    }
}
