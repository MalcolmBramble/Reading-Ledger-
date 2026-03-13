package com.readingledger.app.ui.form

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readingledger.app.data.*
import com.readingledger.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookFormScreen(bookId: String?, onDone: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val scope = rememberCoroutineScope()
    val library by dataStore.libraryFlow.collectAsState(initial = LibraryData())

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES.first()) }
    var status by remember { mutableStateOf(STATUS_OPTIONS.first().first) }
    var pages by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coreArg by remember { mutableStateOf("") }
    var impact by remember { mutableStateOf("") }
    var recommendedBy by remember { mutableStateOf("") }

    var isLoaded by remember { mutableStateOf(false) }

    if (bookId != null && !isLoaded && library.books.isNotEmpty()) {
        library.books.find { it.id == bookId }?.let { book ->
            title = book.title
            author = book.author
            category = book.category
            status = book.status
            pages = if (book.pages > 0) book.pages.toString() else ""
            startDate = book.startDate ?: ""
            endDate = book.endDate ?: ""
            notes = book.notes
            coreArg = book.coreArgument
            impact = book.impact
            recommendedBy = book.recommendedBy
            isLoaded = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Bg),
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (bookId != null) "Edit Book" else "Add Book",
                        color = TextPrimary, fontSize = 20.sp,
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg),
                modifier = Modifier.safeDrawingPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            FormField("Title *", title, { title = it }, "Book title")
            Spacer(Modifier.height(12.dp))

            FormField("Author", author, { author = it }, "Author name")
            Spacer(Modifier.height(12.dp))

            FormLabel("Category")
            Box(modifier = Modifier.fillMaxWidth()) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedCard(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text(category, color = TextPrimary, modifier = Modifier.padding(16.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Surface2)
                ) {
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = TextPrimary) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            FormLabel("Status")
            Box(modifier = Modifier.fillMaxWidth()) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedCard(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border)
                ) {
                    val label = STATUS_OPTIONS.find { it.first == status }?.second ?: status
                    Text(label, color = TextPrimary, modifier = Modifier.padding(16.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Surface2)
                ) {
                    STATUS_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.second, color = TextPrimary) },
                            onClick = {
                                status = opt.first
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            FormField("Pages", pages, { pages = it }, "Number of pages", KeyboardType.Number)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Started")
                    DatePickerField(startDate) { startDate = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Finished")
                    DatePickerField(endDate) { endDate = it }
                }
            }
            Spacer(Modifier.height(12.dp))

            FormField("Notes", notes, { notes = it }, "Your notes...", multiline = true)
            Spacer(Modifier.height(12.dp))

            FormField("Core Argument", coreArg, { coreArg = it }, "What is the book's thesis?", multiline = true)
            Spacer(Modifier.height(12.dp))

            FormField("Impact", impact, { impact = it }, "How did this book affect you?", multiline = true)
            Spacer(Modifier.height(12.dp))

            FormField("Recommended By", recommendedBy, { recommendedBy = it }, "Who recommended this?")
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        val now = isoNow()
                        val finalPages = pages.toIntOrNull() ?: 0
                        var finalStartDate = startDate.ifBlank { null }
                        var finalEndDate = endDate.ifBlank { null }
                        var finalCurrentPage = 0

                        if (status == "reading" && finalStartDate == null) finalStartDate = today()
                        if (status == "completed") {
                            if (finalStartDate == null) finalStartDate = today()
                            if (finalEndDate == null) finalEndDate = today()
                            finalCurrentPage = finalPages
                        }

                        val existingBook = library.books.find { it.id == bookId }
                        val book = existingBook?.copy(
                            title = title.trim(),
                            author = author.trim(),
                            category = category,
                            status = status,
                            pages = finalPages,
                            currentPage = if (status == "completed") finalPages else existingBook.currentPage,
                            startDate = finalStartDate,
                            endDate = finalEndDate,
                            notes = notes.trim(),
                            coreArgument = coreArg.trim(),
                            impact = impact.trim(),
                            recommendedBy = recommendedBy.trim(),
                            updatedAt = now
                        ) ?: Book(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            author = author.trim(),
                            category = category,
                            status = status,
                            pages = finalPages,
                            currentPage = finalCurrentPage,
                            startDate = finalStartDate,
                            endDate = finalEndDate,
                            addedAt = now,
                            updatedAt = now,
                            notes = notes.trim(),
                            coreArgument = coreArg.trim(),
                            impact = impact.trim(),
                            recommendedBy = recommendedBy.trim()
                        )

                        if (bookId != null) dataStore.updateBook(book)
                        else dataStore.addBook(book)
                        
                        onDone()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (bookId != null) "Save Changes" else "Add to Library", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(text, color = TextMedium, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit, hint: String, keyboardType: KeyboardType = KeyboardType.Text, multiline: Boolean = false) {
    Column {
        FormLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, color = TextDim) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = if (multiline) 3 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun DatePickerField(value: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    if (value.isNotEmpty()) {
        try {
            val parts = value.split("-")
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        } catch (e: Exception) {}
    }
    val datePickerDialog = DatePickerDialog(context, { _, year, month, dayOfMonth ->
        onDateSelected(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth))
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    OutlinedCard(
        onClick = { datePickerDialog.show() },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border)
    ) {
        Text(if (value.isEmpty()) "Pick date" else fmtDate(value), color = if (value.isEmpty()) TextDim else TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
    }
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun isoNow(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
private fun fmtDate(iso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        if (date != null) SimpleDateFormat("MMM d, yyyy", Locale.US).format(date) else iso
    } catch (e: Exception) { iso }
}
