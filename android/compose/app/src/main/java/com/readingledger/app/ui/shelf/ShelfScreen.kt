package com.readingledger.app.ui.shelf

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.data.Book
import com.readingledger.app.data.DataStore
import com.readingledger.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ShelfScreen(
    onBookClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val library by dataStore.libraryFlow.collectAsState(initial = com.readingledger.app.data.LibraryData())
    val scope = rememberCoroutineScope()

    var currentSort by remember { mutableStateOf("Date") }
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Header row with Search + Settings
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("PERSONAL LIBRARY", color = AccentDim, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text("The Reading Ledger", color = TextPrimary, fontSize = 28.sp, fontFamily = FontFamily.Serif)
                }
                Row {
                    IconButton(onClick = { searchVisible = !searchVisible; if (!searchVisible) searchQuery = "" }) {
                        Icon(Icons.Default.Search, "Search", tint = TextMedium)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextMedium)
                    }
                }
            }

            // Search bar
            if (searchVisible) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search books...", color = TextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // Goal pill
            val completed = library.books.count { it.status == "completed" }
            val goal = library.settings.goal
            GoalPill(completed, goal)

            Spacer(Modifier.height(24.dp))

            // Filter books by search
            val filteredBooks = if (searchQuery.isBlank()) library.books else {
                val q = searchQuery.lowercase()
                library.books.filter { it.title.lowercase().contains(q) || it.author.lowercase().contains(q) }
            }

            if (filteredBooks.isNotEmpty()) {
                // Now Reading cards
                val readingBooks = filteredBooks.filter { it.status == "reading" }
                if (readingBooks.isNotEmpty()) {
                    Text(
                        "NOW READING",
                        color = TextDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    readingBooks.forEach { book ->
                        NowReadingCard(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            onPageChange = { newPage ->
                                scope.launch {
                                    val updated = if (newPage >= book.pages) {
                                        book.copy(
                                            currentPage = book.pages,
                                            status = "completed",
                                            endDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
                                            updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                                        )
                                    } else {
                                        book.copy(
                                            currentPage = newPage.coerceIn(0, book.pages),
                                            updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                                        )
                                    }
                                    dataStore.updateBook(updated)
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Sort bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${filteredBooks.size} books", color = TextDim, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Date", "Rating", "Length", "Status", "Title", "Category").forEach { label ->
                            val active = currentSort == label
                            Surface(
                                onClick = { currentSort = label },
                                shape = RoundedCornerShape(16.dp),
                                color = if (active) Accent else Color.Transparent,
                                border = if (!active) BorderStroke(1.dp, Border) else null,
                            ) {
                                Text(
                                    label,
                                    color = if (active) Bg else TextMedium,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Bookshelf Logic (6 per row)
                val sortedBooks = when(currentSort) {
                    "Title" -> filteredBooks.sortedBy { it.title }
                    "Rating" -> filteredBooks.sortedByDescending { it.rating }
                    "Length" -> filteredBooks.sortedByDescending { it.pages }
                    "Status" -> filteredBooks.sortedBy { when(it.status) { "reading" -> 0; "want-to-read" -> 1; "completed" -> 2; else -> 3 } }
                    "Category" -> filteredBooks.sortedBy { it.category }
                    else -> filteredBooks.sortedByDescending { it.updatedAt }
                }

                sortedBooks.chunked(6).forEach { rowBooks ->
                    ShelfRow(rowBooks, onBookClick)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(80.dp))
            } else {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (searchQuery.isNotBlank()) "No books match your search." else "Your shelves are empty.",
                        color = TextMedium, fontSize = 20.sp,
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                    )
                    if (searchQuery.isBlank()) {
                        Text("Add your first book to begin.", color = TextDim, fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onAddClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Text("+ Add a Book", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Floating Action Button
        LargeFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .safeDrawingPadding(),
            containerColor = Accent,
            contentColor = Bg,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, "Add Book", modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun NowReadingCard(book: Book, onClick: () -> Unit, onPageChange: (Int) -> Unit) {
    val catColor = CatColors.color(book.category)
    val pct = book.progressPercent

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        border = BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with accent bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(catColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, color = TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    Text(book.author, color = TextDim, fontSize = 13.sp)
                }
                Text("$pct%", color = catColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = Surface2
            )

            Spacer(Modifier.height(12.dp))

            // Page stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageStepButton("-10") { onPageChange(book.currentPage - 10) }
                Spacer(Modifier.width(4.dp))
                PageStepButton("-1") { onPageChange(book.currentPage - 1) }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${book.currentPage}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.width(12.dp))
                PageStepButton("+1") { onPageChange(book.currentPage + 1) }
                Spacer(Modifier.width(4.dp))
                PageStepButton("+10") { onPageChange(book.currentPage + 10) }
            }

            Text(
                "of ${book.pages} pages",
                color = TextDim,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PageStepButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = Surface2,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(label, color = TextMedium, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GoalPill(completed: Int, goal: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            val progress = if (goal > 0) completed.toFloat() / goal else 0f
            Canvas(modifier = Modifier.size(24.dp)) {
                drawCircle(color = Border, style = Stroke(width = 2.dp.toPx()))
                rotate(-90f) {
                    drawArc(
                        color = Accent,
                        startAngle = 0f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("$completed/$goal", color = TextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShelfRow(books: List<Book>, onClick: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            books.forEach { book ->
                BookSpine(book, onClick)
            }
            repeat(6 - books.size) {
                Spacer(Modifier.width(58.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(Color(0xFF2A2520))
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun BookSpine(book: Book, onClick: (String) -> Unit) {
    val textMeasurer = rememberTextMeasurer()
    val spineColor = CatColors.spine(book.category)
    val catColor = CatColors.color(book.category)

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .width(54.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(spineColor, spineColor.copy(alpha = 0.7f)),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            )
            .border(0.5.dp, catColor.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
            .clickable { onClick(book.id) }
            .drawWithContent {
                drawContent()

                if (book.rating >= 4) {
                    drawRect(color = Gold, size = Size(size.width - 8.dp.toPx(), 3.dp.toPx()), topLeft = Offset(4.dp.toPx(), 0f))
                }

                rotate(90f) {
                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(book.title),
                        style = TextStyle(color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontFamily = FontFamily.Serif),
                        maxLines = 1,
                        softWrap = false
                    )
                    drawText(
                        textLayoutResult,
                        topLeft = Offset(size.height / 2 - textLayoutResult.size.width / 2, -size.width / 2 - textLayoutResult.size.height / 2 + 4.dp.toPx())
                    )
                }

                if (book.status == "reading") {
                    drawCircle(color = Accent, radius = 3.dp.toPx(), center = Offset(size.width / 2, size.height - 10.dp.toPx()))
                }
            }
    )
}
