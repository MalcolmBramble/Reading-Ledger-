package com.readingledger.app.ui.shelf

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.ui.theme.*

/**
 * ShelfScreen — the main library view.
 *
 * TODO: Wire up DataStore/ViewModel. This is the structural scaffold
 * showing how the Compose UI maps to the web app's shelf.js.
 *
 * Key composables to build out:
 * - BookSpine (Canvas-drawn spine matching the web app aesthetic)
 * - NowReadingCard (progress stepper)
 * - SortPillRow (horizontal scroll of filter chips)
 * - EmptyState
 */
@Composable
fun ShelfScreen(
    onBookClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Spacer(Modifier.height(8.dp))
        Text("PERSONAL LIBRARY", color = AccentDim, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        Text("The Reading Ledger", color = TextPrimary, fontSize = 28.sp, fontFamily = FontFamily.Serif)
        Spacer(Modifier.height(12.dp))

        // Goal pill — TODO: pull from DataStore
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Surface,
            border = BorderStroke(1.dp, Border),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                // TODO: Canvas progress ring
                Text("0/50", color = TextMedium, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Sort pills — TODO: make interactive
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Date", "Rating", "Length", "Status", "Title").forEach { label ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (label == "Date") Accent else Color.Transparent,
                    border = if (label != "Date") BorderStroke(1.dp, Border) else null,
                ) {
                    Text(
                        label,
                        color = if (label == "Date") Bg else TextMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        // Empty state — TODO: conditionally show based on book count
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Your shelves are empty.",
                color = TextMedium, fontSize = 20.sp,
                fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
            )
            Text("Add your first book to begin.", color = TextDim, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("+ Add a Book", fontWeight = FontWeight.Bold)
            }
        }
    }
}
