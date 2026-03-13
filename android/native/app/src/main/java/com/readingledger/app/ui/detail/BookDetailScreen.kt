package com.readingledger.app.ui.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.readingledger.app.data.Session
import com.readingledger.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val library by dataStore.libraryFlow.collectAsState(initial = com.readingledger.app.data.LibraryData())
    val book = library.books.find { it.id == bookId }
    val scope = rememberCoroutineScope()

    if (book == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Bg),
        containerColor = Bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Accent, fontSize = 16.sp)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Accent)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            dataStore.deleteBook(bookId)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            val catColor = CatColors.color(book.category)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(book.category, catColor)
                Badge(book.status.replace("-", " ").replaceFirstChar { it.uppercase() }, statusColor(book.status))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = book.title,
                color = TextPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
            if (book.author.isNotEmpty()) {
                Text(
                    text = "by ${book.author}",
                    color = TextMedium,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    Text(
                        text = if (i <= book.rating) "★" else "☆",
                        color = if (i <= book.rating) Gold else TextDim,
                        fontSize = 24.sp,
                        modifier = Modifier.clickable {
                            scope.launch {
                                dataStore.updateBook(book.copy(rating = i, updatedAt = isoNow()))
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (book.pages > 0) MetaItem("Pages", book.pages.toString())
            book.startDate?.let { MetaItem("Started", fmtDate(it)) }
            book.endDate?.let { MetaItem("Finished", fmtDate(it)) }
            if (book.recommendedBy.isNotEmpty()) MetaItem("Recommended by", book.recommendedBy)

            if (book.status == "reading" && book.pages > 0) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Progress")
                val pct = book.progressPercent
                Text(
                    "${book.currentPage} / ${book.pages} pages ($pct%)",
                    color = TextMedium,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = pct / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = catColor,
                    trackColor = Surface2
                )
                
                Spacer(Modifier.height(16.dp))
                ReadingTimer(book, onSaveSession = { session ->
                    scope.launch {
                        val updatedSessions = book.sessions + session
                        dataStore.updateBook(book.copy(sessions = updatedSessions, updatedAt = isoNow()))
                    }
                })
            }

            if (book.notes.isNotEmpty()) {
                DetailSection("Notes", book.notes)
            }
            if (book.coreArgument.isNotEmpty()) {
                DetailSection("Core Argument", book.coreArgument, isItalic = true)
            }
            if (book.impact.isNotEmpty()) {
                DetailSection("Impact", book.impact)
            }

            if (book.quotes.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Quotes (${book.quotes.size})")
                book.quotes.forEach { quote ->
                    QuoteCard(quote)
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (book.sessions.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Reading Sessions")
                book.sessions.reversed().forEach { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fmtShort(session.date), color = TextDim, fontSize = 13.sp, modifier = Modifier.width(80.dp))
                        Text("${session.pagesRead} pages · ${session.duration} min", color = TextMedium, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MetaItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = TextDim, fontSize = 13.sp)
        Text(value, color = TextMedium, fontSize = 13.sp)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = TextDim,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DetailSection(title: String, content: String, isItalic: Boolean = false) {
    Spacer(Modifier.height(24.dp))
    SectionTitle(title)
    Text(
        text = content,
        color = TextMedium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontFamily = if (isItalic) FontFamily.Serif else FontFamily.Default,
        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
    )
}

@Composable
fun QuoteCard(quote: com.readingledger.app.data.Quote) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Surface,
        border = BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "“${quote.text}”",
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                lineHeight = 20.sp
            )
            if (quote.page.isNotEmpty()) {
                Text(
                    text = "p. ${quote.page}",
                    color = TextDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ReadingTimer(book: Book, onSaveSession: (Session) -> Unit) {
    var timerRunning by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                seconds++
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reading Timer", color = TextDim, fontSize = 12.sp)
            Text(
                text = String.format("%d:%02d", seconds / 60, seconds % 60),
                color = TextPrimary,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { timerRunning = !timerRunning },
                    colors = ButtonDefaults.buttonColors(containerColor = if (timerRunning) Red.copy(alpha = 0.2f) else Accent.copy(alpha = 0.2f), contentColor = if (timerRunning) Red else Accent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (timerRunning) "Pause" else if (seconds > 0) "Resume" else "Start")
                }
                if (seconds > 0) {
                    Button(
                        onClick = {
                            timerRunning = false
                            val duration = (seconds / 60).coerceAtLeast(1)
                            onSaveSession(Session(date = today(), duration = duration, pagesRead = 0))
                            seconds = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Session")
                    }
                }
            }
        }
    }
}

fun statusColor(status: String) = when(status) {
    "completed" -> Green
    "reading" -> Blue
    "abandoned" -> Red
    else -> TextDim
}

fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
fun isoNow(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
fun fmtDate(iso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(date)
    } catch (e: Exception) { iso }
}
fun fmtShort(iso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        SimpleDateFormat("MMM d", Locale.US).format(date)
    } catch (e: Exception) { iso }
}
