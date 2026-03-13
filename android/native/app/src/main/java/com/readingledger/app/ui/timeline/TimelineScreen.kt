package com.readingledger.app.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.data.DataStore
import com.readingledger.app.data.LibraryData
import com.readingledger.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class TimelineEvent(
    val date: String,
    val action: String,
    val bookTitle: String,
    val bookId: String,
    val category: String,
    val type: String
)

@Composable
fun TimelineScreen(onBookClick: (String) -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val library by dataStore.libraryFlow.collectAsState(initial = LibraryData())

    val events = remember(library.books) {
        val list = mutableListOf<TimelineEvent>()
        library.books.forEach { b ->
            if (b.addedAt.isNotEmpty())
                list.add(TimelineEvent(b.addedAt, "Added", b.title, b.id, b.category, "added"))
            b.startDate?.let {
                if (it.isNotEmpty())
                    list.add(TimelineEvent(it, "Started reading", b.title, b.id, b.category, "started"))
            }
            b.endDate?.let {
                if (it.isNotEmpty()) {
                    val verb = if (b.status == "abandoned") "Dropped" else "Finished"
                    list.add(TimelineEvent(it, verb, b.title, b.id, b.category, if (b.status == "completed") "finished" else "dropped"))
                }
            }
            b.sessions.forEach { s ->
                if (s.date.isNotEmpty()) {
                    list.add(TimelineEvent(s.date, "Read ${s.pagesRead} pages (${s.duration} min)", b.title, b.id, b.category, "session"))
                }
            }
        }
        list.sortedByDescending { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Timeline", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        
        if (events.isEmpty()) {
            Spacer(Modifier.height(60.dp))
            Text(
                "No activity yet.\nStart reading to see your timeline.",
                color = TextDim, fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 20.sp
            )
        } else {
            var lastMonth = ""
            events.take(50).forEach { ev ->
                val month = if (ev.date.length >= 7) ev.date.substring(0, 7) else ""
                if (month != lastMonth) {
                    lastMonth = month
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = fmtMonth(month),
                        color = TextMedium,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                TimelineItem(ev, onBookClick)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun TimelineItem(ev: TimelineEvent, onBookClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClick(ev.bookId) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        val dotColor = when (ev.type) {
            "finished" -> Green
            "dropped" -> Red
            else -> CatColors.color(ev.category)
        }

        Canvas(modifier = Modifier.size(16.dp).padding(top = 4.dp)) {
            drawCircle(color = dotColor, radius = 4.dp.toPx())
        }

        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(ev.action, color = TextMedium, fontSize = 12.sp)
            Text(ev.bookTitle, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Text(fmtShortDate(ev.date), color = TextDim, fontSize = 11.sp)
        }
    }
}

private fun fmtMonth(yearMonth: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth)
        if (date != null) SimpleDateFormat("MMMM yyyy", Locale.US).format(date) else yearMonth
    } catch (e: Exception) { yearMonth }
}

private fun fmtShortDate(iso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        if (date != null) SimpleDateFormat("MMM d", Locale.US).format(date) else iso
    } catch (e: Exception) { iso }
}
