package com.readingledger.app.ui.shelf

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun ShelfScreen(
    onBookClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val library by dataStore.libraryFlow.collectAsState(initial = com.readingledger.app.data.LibraryData())
    
    var currentSort by remember { mutableStateOf("Date") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("PERSONAL LIBRARY", color = AccentDim, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text("The Reading Ledger", color = TextPrimary, fontSize = 28.sp, fontFamily = FontFamily.Serif)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Settings", tint = TextMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            val completed = library.books.count { it.status == "completed" }
            val goal = library.settings.goal
            GoalPill(completed, goal)

            Spacer(Modifier.height(24.dp))

            if (library.books.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${library.books.size} books", color = TextDim, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Date", "Rating", "Length", "Status", "Title").forEach { label ->
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

                val sortedBooks = when(currentSort) {
                    "Title" -> library.books.sortedBy { it.title }
                    "Rating" -> library.books.sortedByDescending { it.rating }
                    "Length" -> library.books.sortedByDescending { it.pages }
                    else -> library.books.sortedByDescending { it.updatedAt }
                }

                sortedBooks.chunked(6).forEach { rowBooks ->
                    ShelfRow(rowBooks, onBookClick)
                    Spacer(Modifier.height(16.dp))
                }
                
                Spacer(Modifier.height(80.dp))
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Your shelves are empty.",
                        color = TextMedium, fontSize = 20.sp,
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                    )
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
