package com.readingledger.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String = "",
    var title: String = "",
    var author: String = "",
    var category: String = "Other",
    var status: String = "want-to-read", // want-to-read, reading, completed, abandoned
    var pages: Int = 0,
    var currentPage: Int = 0,
    var rating: Int = 0,
    var startDate: String? = null,
    var endDate: String? = null,
    val addedAt: String = "",
    var updatedAt: String = "",
    var notes: String = "",
    var coreArgument: String = "",
    var impact: String = "",
    var recommendedBy: String = "",
    var recommendationNote: String = "",
    var recommendationSource: String = "",
    var priority: Int = 0,
    var themes: List<String> = emptyList(),
    var quotes: List<Quote> = emptyList(),
    var connections: List<String> = emptyList(),
    var sessions: List<Session> = emptyList(),
) {
    val progressPercent: Int
        get() = if (pages <= 0) 0 else (currentPage.toFloat() / pages * 100).toInt().coerceIn(0, 100)
}

@Serializable
data class Quote(
    val text: String = "",
    val page: String = "",
    val note: String = "",
)

@Serializable
data class Session(
    val date: String = "",
    val duration: Int = 0,
    val pagesRead: Int = 0,
    val note: String = "",
)

@Serializable
data class LibraryData(
    val books: List<Book> = emptyList(),
    val settings: Settings = Settings(),
)

@Serializable
data class Settings(
    val goal: Int = 50,
    val lastExport: String? = null,
    val goals: Goals = Goals(),
)

@Serializable
data class Goals(
    val annual: Int = 50,
    val categories: Map<String, Int> = emptyMap(),
    val challenges: List<Challenge> = emptyList(),
)

@Serializable
data class Challenge(
    val id: String = "",
    val label: String = "",
    val type: String = "count",
    val target: Int = 1,
    val categoryFilter: String? = null,
    val minPages: Int? = null,
    val deadline: String? = null,
)

val CATEGORIES = listOf(
    "Self-Awareness", "Current America", "Economics & Money", "Technology",
    "American History", "Science", "World History", "Philosophy & Ethics",
    "Religion", "Fiction", "Other"
)

val STATUS_OPTIONS = listOf(
    "want-to-read" to "To Read",
    "reading" to "Reading",
    "completed" to "Completed",
    "abandoned" to "Dropped",
)
