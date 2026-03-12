package com.readingledger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ═══ Brand Colors ═══
val Bg = Color(0xFF0E0C0A)
val Surface = Color(0xFF1A1714)
val Surface2 = Color(0xFF23201B)
val Border = Color(0xFF2E2A24)
val TextPrimary = Color(0xFFE8E0D4)
val TextMedium = Color(0xFFA89E8C)
val TextDim = Color(0xFF6B6459)
val Accent = Color(0xFFA8B88E)
val AccentDim = Color(0xFF7D8B6E)
val Gold = Color(0xFFD4A847)
val Green = Color(0xFF6EC4A7)
val Blue = Color(0xFF5B9EAD)
val Red = Color(0xFFC46B5B)

// ═══ Category Colors ═══
object CatColors {
    val map = mapOf(
        "Self-Awareness" to Color(0xFFC4956A),
        "Current America" to Color(0xFF7D8B6E),
        "Economics & Money" to Color(0xFF3D5A80),
        "Technology" to Color(0xFFC46B5B),
        "American History" to Color(0xFFC45B72),
        "Science" to Color(0xFF8B6AAC),
        "World History" to Color(0xFF5B9EAD),
        "Philosophy & Ethics" to Color(0xFF6EC4A7),
        "Religion" to Color(0xFF9B7EC4),
        "Fiction" to Color(0xFFBBA14F),
        "Other" to Color(0xFF7A7670),
    )
    val spineMap = mapOf(
        "Self-Awareness" to Color(0xFF8B6540),
        "Current America" to Color(0xFF556B4A),
        "Economics & Money" to Color(0xFF2E4460),
        "Technology" to Color(0xFF8B4B3B),
        "American History" to Color(0xFF8B3B4E),
        "Science" to Color(0xFF5E4570),
        "World History" to Color(0xFF3B7080),
        "Philosophy & Ethics" to Color(0xFF408060),
        "Religion" to Color(0xFF6B5090),
        "Fiction" to Color(0xFF8B7530),
        "Other" to Color(0xFF5A5650),
    )
    fun color(cat: String) = map[cat] ?: map["Other"]!!
    fun spine(cat: String) = spineMap[cat] ?: spineMap["Other"]!!
}

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Bg,
    secondary = AccentDim,
    background = Bg,
    surface = Surface,
    surfaceVariant = Surface2,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMedium,
    outline = Border,
    error = Red,
)

@Composable
fun ReadingLedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
