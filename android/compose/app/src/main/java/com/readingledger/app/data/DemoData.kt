package com.readingledger.app.data

import java.text.SimpleDateFormat
import java.util.*

object DemoData {
    private fun uid(): String = UUID.randomUUID().toString()
    private fun isoNow(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

    fun createLibrary(): LibraryData {
        val now = isoNow()
        return LibraryData(
            books = listOf(
                Book(
                    id = uid(), title = "Atomic Habits", author = "James Clear",
                    category = "Self-Awareness", status = "completed", pages = 320, currentPage = 320,
                    rating = 5, startDate = "2025-01-05", endDate = "2025-01-18",
                    notes = "Remarkable framework for behavior change.",
                    themes = listOf("habits", "systems", "identity"),
                    quotes = listOf(
                        Quote("You do not rise to the level of your goals. You fall to the level of your systems.", "27"),
                        Quote("Every action you take is a vote for the type of person you wish to become.", "38")
                    ),
                    sessions = listOf(
                        Session("2025-01-08", 60, 100),
                        Session("2025-01-12", 75, 120),
                        Session("2025-01-17", 55, 100)
                    ),
                    coreArgument = "Small habits compound into remarkable results over time.",
                    impact = "Changed how I think about daily routines.",
                    addedAt = "2025-01-05T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "Thinking, Fast and Slow", author = "Daniel Kahneman",
                    category = "Science", status = "completed", pages = 499, currentPage = 499,
                    rating = 5, startDate = "2025-02-01", endDate = "2025-03-02",
                    notes = "Dense but essential on cognitive biases.",
                    themes = listOf("cognition", "bias", "decision-making"),
                    quotes = listOf(
                        Quote("Nothing in life is as important as you think it is, while you are thinking about it.", "402")
                    ),
                    sessions = listOf(
                        Session("2025-02-10", 120, 180),
                        Session("2025-02-20", 90, 170),
                        Session("2025-03-01", 80, 149)
                    ),
                    coreArgument = "Two systems govern thought: fast intuition and slow deliberation.",
                    addedAt = "2025-02-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "The Wealth of Nations", author = "Adam Smith",
                    category = "Economics & Money", status = "completed", pages = 1264, currentPage = 1264,
                    rating = 4, startDate = "2024-11-01", endDate = "2025-01-15",
                    notes = "Foundational text of modern economics.",
                    themes = listOf("capitalism", "markets", "labor"),
                    coreArgument = "Free markets guided by self-interest produce collective prosperity.",
                    addedAt = "2024-11-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "Sapiens", author = "Yuval Noah Harari",
                    category = "World History", status = "completed", pages = 443, currentPage = 443,
                    rating = 4, startDate = "2025-03-10", endDate = "2025-04-05",
                    notes = "Sweeping narrative of human history.",
                    themes = listOf("civilization", "evolution", "narrative"),
                    quotes = listOf(
                        Quote("The real difference between us and chimpanzees is the mythical glue that binds together large numbers of individuals.", "25")
                    ),
                    sessions = listOf(
                        Session("2025-03-15", 90, 150),
                        Session("2025-03-25", 100, 170),
                        Session("2025-04-04", 70, 123)
                    ),
                    coreArgument = "Shared myths enable human cooperation at scale.",
                    impact = "Reframed how I understand institutions.",
                    addedAt = "2025-03-10T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "Meditations", author = "Marcus Aurelius",
                    category = "Philosophy & Ethics", status = "completed", pages = 256, currentPage = 256,
                    rating = 5, startDate = "2025-04-10", endDate = "2025-04-20",
                    notes = "Timeless. Every page has something to sit with.",
                    themes = listOf("stoicism", "virtue", "mortality"),
                    quotes = listOf(
                        Quote("The happiness of your life depends upon the quality of your thoughts.", "")
                    ),
                    sessions = listOf(
                        Session("2025-04-12", 45, 128),
                        Session("2025-04-19", 50, 128)
                    ),
                    coreArgument = "Inner peace through acceptance of what you cannot control.",
                    impact = "Daily reference now.",
                    addedAt = "2025-04-10T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "1984", author = "George Orwell",
                    category = "Fiction", status = "completed", pages = 328, currentPage = 328,
                    rating = 4, startDate = "2025-05-01", endDate = "2025-05-12",
                    notes = "Chillingly relevant.",
                    themes = listOf("totalitarianism", "surveillance", "language"),
                    sessions = listOf(
                        Session("2025-05-05", 80, 160),
                        Session("2025-05-11", 85, 168)
                    ),
                    addedAt = "2025-05-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "The Gene", author = "Siddhartha Mukherjee",
                    category = "Science", status = "reading", pages = 592, currentPage = 340,
                    rating = 0, startDate = "2026-02-15",
                    notes = "Beautifully written history of genetics.",
                    themes = listOf("genetics", "science", "ethics"),
                    sessions = listOf(
                        Session("2026-02-20", 90, 120),
                        Session("2026-02-25", 75, 120),
                        Session("2026-03-05", 60, 100)
                    ),
                    addedAt = "2026-02-15T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "Democracy in America", author = "Alexis de Tocqueville",
                    category = "American History", status = "reading", pages = 864, currentPage = 210,
                    rating = 0, startDate = "2026-03-01",
                    themes = listOf("democracy", "America", "institutions"),
                    sessions = listOf(
                        Session("2026-03-05", 120, 100),
                        Session("2026-03-09", 80, 110)
                    ),
                    addedAt = "2026-03-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "The Innovators", author = "Walter Isaacson",
                    category = "Technology", status = "want-to-read", pages = 542,
                    addedAt = "2026-01-15T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "The Righteous Mind", author = "Jonathan Haidt",
                    category = "Current America", status = "want-to-read", pages = 419,
                    addedAt = "2026-02-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "The Brothers Karamazov", author = "Fyodor Dostoevsky",
                    category = "Fiction", status = "want-to-read", pages = 796,
                    addedAt = "2026-03-01T10:00:00Z", updatedAt = now
                ),
                Book(
                    id = uid(), title = "Being and Time", author = "Martin Heidegger",
                    category = "Philosophy & Ethics", status = "abandoned", pages = 589, currentPage = 95,
                    rating = 2, startDate = "2025-06-01", endDate = "2025-06-20",
                    notes = "Too dense. Will revisit.",
                    themes = listOf("existentialism"),
                    addedAt = "2025-06-01T10:00:00Z", updatedAt = now
                )
            ),
            settings = Settings(goal = 50, goals = Goals(annual = 50))
        )
    }
}
