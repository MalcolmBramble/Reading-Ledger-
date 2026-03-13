package com.readingledger.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.readingledger.app.data.DataStore
import com.readingledger.app.data.LibraryData
import com.readingledger.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val scope = rememberCoroutineScope()
    val library by dataStore.libraryFlow.collectAsState(initial = LibraryData())

    var annualGoalText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDemoDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(library.settings.goal) {
        annualGoalText = library.settings.goal.toString()
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                if (content != null) {
                    pendingImportJson = content
                    showImportDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Surface,
            title = { Text("Clear Everything", color = TextPrimary) },
            text = { Text("This will permanently delete all books, sessions, and settings. This cannot be undone.", color = TextMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    scope.launch {
                        dataStore.clearAll()
                        Toast.makeText(context, "Library cleared", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Clear", color = Red) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = TextMedium) }
            }
        )
    }

    // Demo data confirmation dialog
    if (showDemoDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDialog = false },
            containerColor = Surface,
            title = { Text("Reload Sample Data", color = TextPrimary) },
            text = { Text("This will replace your current library with sample data. Continue?", color = TextMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showDemoDialog = false
                    scope.launch {
                        dataStore.loadDemoData()
                        Toast.makeText(context, "Sample data loaded", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Reload", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDemoDialog = false }) { Text("Cancel", color = TextMedium) }
            }
        )
    }

    // Import mode dialog (Replace / Merge / Cancel)
    if (showImportDialog && pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false; pendingImportJson = null },
            containerColor = Surface,
            title = { Text("Import Library", color = TextPrimary) },
            text = { Text("How would you like to import?", color = TextMedium) },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showImportDialog = false
                        scope.launch {
                            val ok = dataStore.importJson(pendingImportJson!!, merge = false)
                            Toast.makeText(context, if (ok) "Library replaced" else "Import failed", Toast.LENGTH_SHORT).show()
                            pendingImportJson = null
                        }
                    }) { Text("Replace", color = Red) }
                    TextButton(onClick = {
                        showImportDialog = false
                        scope.launch {
                            val ok = dataStore.importJson(pendingImportJson!!, merge = true)
                            Toast.makeText(context, if (ok) "Library merged" else "Import failed", Toast.LENGTH_SHORT).show()
                            pendingImportJson = null
                        }
                    }) { Text("Merge", color = Accent) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false; pendingImportJson = null }) { Text("Cancel", color = TextMedium) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text("✕", color = TextMedium, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Annual Reading Goal
        SettingsSectionHeader("Annual Reading Goal")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = annualGoalText,
                onValueChange = { annualGoalText = it },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Text("  books per year", color = TextDim, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val g = annualGoalText.toIntOrNull() ?: 0
                    if (g > 0) {
                        scope.launch {
                            dataStore.setAnnualGoal(g)
                            Toast.makeText(context, "Goal set to $g", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Surface2, contentColor = Accent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Set")
            }
        }

        Spacer(Modifier.height(32.dp))

        // Library Management
        SettingsSectionHeader("Library Management")

        SettingsButton("Export Library (JSON)") {
            scope.launch {
                val json = dataStore.exportJson()
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TEXT, json)
                    putExtra(Intent.EXTRA_SUBJECT, "reading-ledger-backup.json")
                }
                context.startActivity(Intent.createChooser(intent, "Export Library"))
            }
        }

        library.settings.lastExport?.let { date ->
            Text(
                "Last export: $date",
                color = TextDim,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        SettingsButton("Import Library (JSON)") {
            importLauncher.launch("application/json")
        }

        Spacer(Modifier.height(32.dp))

        // Sample Data
        SettingsSectionHeader("Sample Data")

        SettingsButton("Reload Sample Data") {
            showDemoDialog = true
        }

        Spacer(Modifier.height(32.dp))

        // Danger Zone
        SettingsSectionHeader("Danger Zone", color = Red)

        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
            border = BorderStroke(1.dp, Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Clear Everything")
        }

        Spacer(Modifier.height(48.dp))

        // App Info
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("The Reading Ledger · Compose v2.0", color = TextDim, fontSize = 11.sp)
            Text("com.readingledger.app", color = TextDim, fontSize = 11.sp)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSectionHeader(text: String, color: Color = TextPrimary) {
    Text(
        text = text,
        color = color,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = Accent),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Text(text)
    }
}
