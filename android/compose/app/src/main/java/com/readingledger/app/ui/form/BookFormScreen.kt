package com.readingledger.app.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.ui.theme.*

@Composable
fun BookFormScreen(bookId: String?, onDone: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(
            if (bookId != null) "Edit Book" else "Add Book",
            color = TextPrimary, fontSize = 22.sp,
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))

        // TODO: Load existing book data if bookId != null
        // TODO: Full form fields matching BookFormActivity.java

        Text("Title *", color = TextMedium, fontSize = 12.sp)
        OutlinedTextField(
            value = title, onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
            )
        )
        Spacer(Modifier.height(12.dp))

        Text("Author", color = TextMedium, fontSize = 12.sp)
        OutlinedTextField(
            value = author, onValueChange = { author = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
            )
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: save to DataStore */ onDone() },
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (bookId != null) "Save Changes" else "Add to Library", fontWeight = FontWeight.Bold)
        }
    }
}
