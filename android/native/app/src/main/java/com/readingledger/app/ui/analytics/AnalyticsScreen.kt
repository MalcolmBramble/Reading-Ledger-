package com.readingledger.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.data.Book
import com.readingledger.app.data.DataStore
import com.readingledger.app.data.LibraryData
import com.readingledger.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val library by dataStore.libraryFlow.collectAsState(initial = LibraryData())

    val completed = library.books.filter { it.status == "completed" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Analytics", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        if (completed.isEmpty()) {
            Spacer(Modifier.height(60.dp))
            Text(
                "Complete at least 1 book\nto see analytics.",
                color = TextMedium, fontSize = 16.sp,
                fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 24.sp
            )
        } else {
            Spacer(Modifier.height(24.dp))
            
            val totalPages = completed.sumOf { it.pages }
            val ratedBooks = completed.filter { it.rating > 0 }
            val avgRating = if (ratedBooks.isNotEmpty()) ratedBooks.map { it.rating }.average() else 0.0
            val totalMinutes = library.books.sumOf { b -> b.sessions.sumOf { it.duration } }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Books Read", completed.size.toString(), Modifier.weight(1f))
                StatCard("Pages", String.format(Locale.US, "%,d", totalPages), Modifier.weight(1f))
                StatCard("Avg Rating", if (avgRating > 0) String.format(Locale.US, "%.1f★", avgRating) else "—", Modifier.weight(1f))
                StatCard("Hours", (totalMinutes / 60).toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("By Category")
            val catCounts = completed.groupBy { it.category }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
            catCounts.forEach { (cat, count) ->
                CatBar(cat, count, completed.size)
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("Ratings")
            val ratingDist = completed.groupBy { it.rating }.mapValues { it.value.size }
            for (r in 5 downTo 1) {
                RatingBar(r, ratingDist.getOrDefault(r, 0), completed.size)
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("Reading Pace")
            val monthlyBooks = completed
                .mapNotNull { it.endDate?.take(7) }
                .groupBy { it }
                .mapValues { it.value.size }
                .toSortedMap()
            
            val maxMonthly = if (monthlyBooks.isNotEmpty()) monthlyBooks.values.max() else 1
            monthlyBooks.forEach { (month, count) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fmtMonth(month), color = TextMedium, fontSize = 12.sp, modifier = Modifier.width(80.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(count.toFloat() / maxMonthly)
                                .height(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Accent)
                        )
                    }
                    Text(" $count", color = TextDim, fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = TextMedium,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextDim, fontSize = 10.sp)
        }
    }
}

@Composable
fun CatBar(cat: String, count: Int, total: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CatColors.color(cat)))
        Text("  $cat", color = TextMedium, fontSize = 12.sp, modifier = Modifier.width(120.dp))
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(count.toFloat() / total)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CatColors.color(cat))
            )
        }
        Text(" $count", color = TextDim, fontSize = 12.sp)
    }
}

@Composable
fun RatingBar(rating: Int, count: Int, total: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        val stars = "★".repeat(rating)
        Text(stars, color = Gold, fontSize = 12.sp, modifier = Modifier.width(60.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (total > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(count.toFloat() / total)
                        .height(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Gold)
                )
            }
        }
        Text(" $count", color = TextDim, fontSize = 12.sp)
    }
}

private fun fmtMonth(yearMonth: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth)
        if (date != null) SimpleDateFormat("MMM", Locale.US).format(date) else yearMonth
    } catch (e: Exception) { yearMonth }
}
