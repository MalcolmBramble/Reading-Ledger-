package com.readingledger.app.ui.settings

import android.content.Intent
import android.widget.Toast
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
    
    LaunchedEffect(library.settings.goal) {
        annualGoalText = library.settings.goal.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
            Text("← Back", color = Accent, fontSize = 16.sp)
        }
        Text(
            text = "Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(24.dp))

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

        SettingsSectionHeader("Library Management")
        
        SettingsButton("Export Library (JSON)") {
            scope.launch {
                val json = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(LibraryData.serializer(), dataStore.getLibrary())
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TEXT, json)
                    putExtra(Intent.EXTRA_SUBJECT, "reading-ledger-backup.json")
                }
                context.startActivity(Intent.createChooser(intent, "Export Library"))
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            "Import functionality requires system file picker integration.",
            color = TextDim, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        SettingsSectionHeader("Danger Zone", color = Red)
        
        OutlinedButton(
            onClick = {
                scope.launch {
                    dataStore.saveLibrary(LibraryData())
                    Toast.makeText(context, "Library cleared", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
            border = BorderStroke(1.dp, Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Clear Everything")
        }

        Spacer(Modifier.height(48.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("The Reading Ledger · Kotlin Compose v1.0", color = TextDim, fontSize = 11.sp)
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
