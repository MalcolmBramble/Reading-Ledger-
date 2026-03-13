package com.readingledger.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Locale

@Composable
fun ReviewScreen(onBookClick: (String) -> Unit) {
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
        Text("Year in Review", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        if (completed.size < 3) {
            Spacer(Modifier.height(60.dp))
            Text(
                "Complete at least 3 books to unlock\nYear in Review.",
                color = TextMedium, fontSize = 16.sp,
                fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 24.sp
            )
        } else {
            Spacer(Modifier.height(24.dp))

            ReviewHeader("Highest Rated")
            val topRated = completed.sortedByDescending { it.rating }.take(3)
            topRated.forEachIndexed { index, book ->
                ReviewBookItem(book, "${index + 1}.", onBookClick)
            }

            Spacer(Modifier.height(32.dp))

            ReviewHeader("Milestones")
            
            val milestones = mutableListOf<Triple<String, String, String>>()
            if (completed.isNotEmpty()) 
                milestones.add(Triple("📖", "First Book", completed.first().title))
            if (completed.size >= 5) 
                milestones.add(Triple("📚", "5 Books", "Reached with ${completed[4].title}"))
            if (completed.size >= 10) 
                milestones.add(Triple("⭐", "10 Books", "Reached with ${completed[9].title}"))
            
            val totalPages = completed.sumOf { it.pages }
            if (totalPages >= 2000)
                milestones.add(Triple("📏", String.format(Locale.US, "%,d Pages", totalPages), "and counting"))
            
            if (completed.any { it.rating == 5 })
                milestones.add(Triple("🌟", "First 5-Star", "Discerning taste"))

            milestones.forEach { (icon, title, desc) ->
                MilestoneItem(icon, title, desc)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(32.dp))

            val uniqueCats = completed.map { it.category }.distinct().size
            Text(
                "Categories explored: $uniqueCats of ${com.readingledger.app.data.CATEGORIES.size}",
                color = TextMedium,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ReviewHeader(text: String) {
    Text(
        text = text,
        color = TextMedium,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ReviewBookItem(book: Book, rank: String, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(book.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rank, color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        Column {
            Text(book.title, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Serif)
            Row {
                if (book.author.isNotEmpty()) {
                    Text(book.author, color = TextDim, fontSize = 12.sp)
                    Text(" · ", color = TextDim, fontSize = 12.sp)
                }
                Text("★".repeat(book.rating), color = Gold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MilestoneItem(icon: String, title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.width(40.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextDim, fontSize = 12.sp)
            }
        }
    }
}
