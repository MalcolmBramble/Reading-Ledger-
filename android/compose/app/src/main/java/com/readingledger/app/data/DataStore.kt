package com.readingledger.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "reading_ledger")

class DataStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
    private val keyData = stringPreferencesKey("library_json")

    val libraryFlow: Flow<LibraryData> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[keyData]
        if (rawJson == null) LibraryData() else {
            try {
                json.decodeFromString<LibraryData>(rawJson)
            } catch (e: Exception) {
                LibraryData()
            }
        }
    }

    suspend fun getLibrary(): LibraryData = libraryFlow.first()

    suspend fun saveLibrary(data: LibraryData) {
        val rawJson = json.encodeToString(data)
        context.dataStore.edit { prefs ->
            prefs[keyData] = rawJson
        }
    }

    suspend fun addBook(book: Book) {
        val current = getLibrary()
        saveLibrary(current.copy(books = current.books + book))
    }

    suspend fun updateBook(book: Book) {
        val current = getLibrary()
        saveLibrary(current.copy(books = current.books.map { if (it.id == book.id) book else it }))
    }

    suspend fun deleteBook(bookId: String) {
        val current = getLibrary()
        saveLibrary(current.copy(books = current.books.filter { it.id != bookId }))
    }

    suspend fun setAnnualGoal(goal: Int) {
        val current = getLibrary()
        saveLibrary(current.copy(settings = current.settings.copy(goal = goal, goals = current.settings.goals.copy(annual = goal))))
    }

    suspend fun exportJson(): String {
        val current = getLibrary()
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
        saveLibrary(current.copy(settings = current.settings.copy(lastExport = now)))
        return json.encodeToString(current)
    }

    suspend fun importJson(jsonStr: String, merge: Boolean): Boolean {
        return try {
            val imported = json.decodeFromString<LibraryData>(jsonStr)
            if (merge) {
                val current = getLibrary()
                val existingKeys = current.books.map { "${it.title}|${it.author}" }.toSet()
                val newBooks = imported.books.filter { "${it.title}|${it.author}" !in existingKeys }
                saveLibrary(current.copy(books = current.books + newBooks))
            } else {
                saveLibrary(imported)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearAll() {
        saveLibrary(LibraryData())
    }

    suspend fun loadDemoData() {
        saveLibrary(DemoData.createLibrary())
    }
}
