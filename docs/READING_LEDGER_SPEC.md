# The Reading Ledger — Project Spec

## Overview
A book tracker Android app (APK) for Samsung Galaxy S24, Android 16. WebView-based architecture: self-contained web app wrapped in a minimal Android shell. All data stored on-device with JSON export/import for backup.

## Target
- Device: Samsung Galaxy S24
- OS: Android 16 (API 35)
- Min SDK: 35 (no backwards compat needed)
- Build: Gradle CLI (no Android Studio required)

---

## Project Structure

```
reading-ledger/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/readingledger/app/
│   │   │   └── MainActivity.kt          # WebView shell (~60 lines)
│   │   ├── assets/
│   │   │   └── index.html               # The entire app (single file, self-contained)
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml           # Edge-to-edge, status bar color
│   │       └── mipmap-xxxhdpi/
│   │           └── ic_launcher.png       # App icon
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts                      # Root build file
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

---

## Architecture

### Android Shell (MainActivity.kt)
Minimal Kotlin activity that:
1. Creates a full-screen WebView (no browser chrome)
2. Loads `file:///android_asset/index.html`
3. Enables JavaScript, DOM storage, file access
4. Registers a JavaScript interface `AndroidBridge` with two methods:
   - `exportData(json: String)` — writes JSON to Downloads folder, triggers share intent
   - `importData()` — opens file picker, reads JSON, passes back to WebView via `javascript:onImportData(json)`
5. Handles back button (WebView history first, then exit)
6. Sets status bar color to match app header (#3D2B1F)

### Web App (index.html)
Single self-contained HTML file with all CSS and JS inlined. No external dependencies except Google Fonts (loaded if online, falls back to system serif/sans if offline).

**Data layer:**
- All book data stored in `localStorage` under key `reading-ledger-v1`
- Data shape: `{ books: Book[], settings: { sort, tab, goal } }`
- Android's WebView preserves localStorage across sessions and app restarts
- On export: calls `AndroidBridge.exportData(JSON.stringify(data))`
- On import: calls `AndroidBridge.importData()`, receives callback with JSON

**No external libraries.** Vanilla JS, no React, no build step. The HTML file IS the app. This keeps the APK tiny (<500KB) and eliminates all bundling complexity.

---

## Data Model

```typescript
interface Book {
  id: string;              // nanoid or timestamp-based
  title: string;
  author: string;
  category: string;        // from predefined list
  pages: number | null;
  currentPage: number;     // 0 if not started
  status: "want-to-read" | "reading" | "completed" | "abandoned";
  rating: number;          // 0-5, 0 = unrated
  notes: string;
  startDate: string;       // ISO date or empty
  endDate: string;         // ISO date or empty
  addedAt: string;         // ISO datetime
  updatedAt: string;       // ISO datetime
}
```

### Categories (with spine colors)
```
Self-Awareness       #8B5E3C
Current America      #6B4423
Economics & Money    #2D5016
Technology           #1A3A5C
American History     #7C2D12
Science              #4A2882
World History        #5C4033
Philosophy & Ethics  #1F4E46
Religion             #6B3A2A
Fiction              #3D1C56
Other                #4A4A4A
```

---

## Views / Screens

### 1. Shelf View (default)
- Book spines on wooden shelf boards, 8 per row
- Spine height scales with page count (60px min, 140px max)
- Spine color = category color
- Visual indicators:
  - Gold top edge → rated 4-5 stars
  - Green pulse dot → currently reading
  - Progress fill overlay → reading progress %
  - Checkmark dot → completed
  - Hollow dot → want-to-read
- Vertical title text on each spine
- Sort pills: Recent | Category | Rating | Length | Status | Title
- Tap spine → opens book detail panel

### 2. List View
- Searchable (title + author)
- Same sort options as shelf
- Each row: mini spine color bar, title, author, status badge, rating stars, page count
- Tap row → opens book detail panel

### 3. Stats View
- **Goal progress bar**: X of 50 books, percentage
- **Stat cards**: Books read, Pages read, Avg rating
- **Category pie chart**: distribution of all books (build with canvas, no library)
- **Reading pace chart**: books completed per month (canvas bar chart)
- **Pages per month chart**: total pages per month (canvas area/bar chart)

### 4. Book Detail Panel (modal overlay)
- Category-colored gradient header
- Title, author, status badge, rating stars
- Progress bar with inline page number input (for currently reading)
- Start/end dates
- Notes section
- Edit / Delete buttons

### 5. Add/Edit Book Form (modal overlay)
- Title, Author, Category (dropdown), Pages
- Status selector (pill buttons)
- Current page (shows when status = reading)
- Start date, End date
- Rating (star selector)
- Notes (textarea)
- Save / Cancel

### 6. Settings (accessible from header)
- Annual goal (number input, default 50)
- Export library (JSON to Downloads + share)
- Import library (file picker, JSON)
- Clear all data (with confirmation)

---

## Header
- App title: "The Reading Ledger" (serif font)
- Subtitle: "X of Y books · 2026"
- Mini progress bar beneath
- Add button (+) on right
- Settings gear icon on right

### Tab Bar (below header)
Three tabs: Shelf | List | Stats

### Now Reading Strip (below tabs, above content)
- Shows all books with status "reading"
- Each: mini spine, title, progress bar with %, tap to detail
- Dark background (#3D2B1F) to visually separate

---

## Design Tokens

```
Background:        #FAF6F0 (aged paper)
Surface:           #F5EFE3 (warm cream)
Header/Dark:       #3D2B1F (dark leather)
Primary:           #5A4428 (rich brown)
Accent:            #D4A030 (gold)
Text Primary:      #3D2B1F
Text Secondary:    #8B7355
Text Muted:        #A89880
Border:            #D4C5A9
Input Background:  #FFFCF5
Shelf Wood:        linear-gradient(180deg, #8B6F47, #6B5335, #5A4428)

Fonts:
  Heading: Georgia, serif (system, no external load needed)
  Body: system sans-serif (-apple-system, sans-serif)
```

---

## Backup/Sync Flow

### Export
1. User taps "Export Library" in settings
2. App serializes entire localStorage data to JSON
3. Calls `AndroidBridge.exportData(jsonString)`
4. Kotlin side: writes `reading-ledger-backup-YYYY-MM-DD.json` to Downloads
5. Kotlin side: fires ACTION_SEND share intent so user can send to Google Drive, email, etc.

### Import
1. User taps "Import Library" in settings
2. App calls `AndroidBridge.importData()`
3. Kotlin side: opens ACTION_OPEN_DOCUMENT file picker filtered to `application/json`
4. Kotlin side: reads file content, calls `webView.evaluateJavascript("onImportData('$escaped')")`
5. JS side: parses JSON, asks "Replace or merge?" via modal
   - Replace: overwrites all data
   - Merge: adds books not already present (match on title+author), keeps existing entries
6. Saves to localStorage

### Auto-backup reminder
- Track last export date in settings
- If > 30 days since last export, show subtle banner: "Back up your library?"

---

## Build Instructions (for Claude Code)

1. Set up Android SDK command-line tools (cmdline-tools, build-tools 35.0.0, platform android-35)
2. Create project structure above
3. Build the index.html first — test in browser until solid
4. Wire up MainActivity.kt with WebView + JavascriptInterface
5. Build APK: `./gradlew assembleDebug`
6. Sign with debug key for sideloading
7. Output: `app/build/outputs/apk/debug/app-debug.apk`

The APK should be under 500KB. User installs via sideload (transfer to phone, tap to install, allow unknown sources).

---

## What NOT to include
- No React, no Vue, no framework — vanilla JS only
- No npm, no node_modules, no bundler
- No external API calls
- No ads, no analytics, no permissions beyond storage
- No backwards compatibility below API 35
- No launcher activity chooser — single activity app
