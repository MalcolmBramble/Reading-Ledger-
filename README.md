# The Reading Ledger

A personal book-tracking app with a tactile, shelf-first design. Track every book you read with color-coded spines on wooden shelves, detailed analytics, reading timers, and a year-in-review that celebrates your progress.

---

## Screenshots

> *Add screenshots here*
>
> Suggested: Shelf view, Book detail, Analytics, Year in Review

---

## Features

**Library & Shelf**
- Book spines rendered on wooden shelves, color-coded by category (11 categories)
- Gold edges on 4★+ books, reading dots on active reads, sort by 6 criteria
- Full-text search across titles, authors, categories, notes, themes, and quotes

**Reading Tracking**
- "Now Reading" cards with page-stepper (+1, +10, −1, −10)
- Built-in reading timer with session logging
- Reading streaks, nudge banners, backup reminders

**Book Detail**
- Star ratings, notes, core argument, personal impact
- Quotes collection with page references
- Themes, connections, recommendations, reading sessions log

**Analytics**
- Books read, total pages, average rating, hours logged
- Category breakdowns, rating distribution, monthly reading pace
- Day streaks, weekly/monthly reading heatmap

**Year in Review**
- Unlocks after 3 completed books
- Top-rated books, 50 auto-generated milestones
- Category diversity tracker

**Data**
- JSON import/export (compatible across web and Android)
- CSV export
- Annual goals, category goals, custom challenges
- Demo library with 12 books for exploring the app

---

## Project Structure

```
Reading-Ledger-/
├── web/                      ← Progressive Web App
│   ├── css/styles.css        ← All styles (~57KB)
│   ├── js/                   ← App logic (~135KB across 8 modules)
│   │   ├── data.js           ← Data model, demo data, milestones
│   │   ├── app.js            ← Search API, add/edit form, settings, wiring
│   │   ├── shelf.js          ← Shelf rendering, spine layout, Now Reading
│   │   ├── detail.js         ← Book detail screen
│   │   ├── timeline.js       ← Timeline view
│   │   ├── analytics.js      ← Analytics charts and stats
│   │   ├── insights.js       ← Year in Review
│   │   └── helpers.js        ← Onboarding, nudges, header, utilities
│   ├── index.html            ← App shell
│   ├── dist.html             ← Single-file build (all CSS+JS inlined)
│   ├── build.sh              ← Builds dist.html from source
│   ├── manifest.json         ← PWA manifest
│   ├── sw.js                 ← Service worker
│   └── icon-*.png / icon.svg ← App icons
│
├── android/
│   ├── native/               ← Java APK (buildable without Android Studio)
│   │   ├── src/              ← Java source (4 activities, data layer, models)
│   │   ├── res/              ← Resources (styles, strings, icon)
│   │   └── AndroidManifest.xml
│   │
│   └── compose/              ← Kotlin + Jetpack Compose (Android Studio)
│       ├── app/src/main/     ← Compose screens, theme, data models
│       ├── build.gradle      ← Dependencies (Compose BOM, Navigation, M3)
│       └── settings.gradle
│
└── docs/
    └── READING_LEDGER_SPEC.md  ← Full design specification
```

---

## Getting Started

### Web App

Open `web/index.html` in a browser, or use the self-contained build:

```bash
cd web
bash build.sh        # Generates dist.html (~178KB, everything inlined)
open dist.html       # Works offline, no server needed
```

The app uses `localStorage` for persistence. Install as a PWA from your browser for a native-like experience.

### Android — Native Java APK

Requires: Java 8+, Android SDK (API 23+), `aapt`, `dx`, `apksigner`

```bash
cd android/native

# 1. Generate R.java
aapt package -f -m -J build \
    -M AndroidManifest.xml -S res \
    -I $ANDROID_HOME/platforms/android-23/android.jar

# 2. Compile
mkdir -p build/classes
javac -source 8 -target 8 \
    -classpath $ANDROID_HOME/platforms/android-23/android.jar \
    -d build/classes -sourcepath "build:src" \
    $(find src -name "*.java") $(find build -name "R.java")

# 3. DEX
dx --dex --min-sdk-version=26 --output=build/classes.dex build/classes/

# 4. Package + Sign
aapt package -f -M AndroidManifest.xml -S res \
    -I $ANDROID_HOME/platforms/android-23/android.jar \
    -F build/app-unsigned.apk
cd build && aapt add app-unsigned.apk classes.dex
zipalign -f 4 app-unsigned.apk app-aligned.apk
apksigner sign --ks debug.keystore --out reading-ledger.apk app-aligned.apk
```

### Android — Kotlin + Compose (Android Studio)

1. Open `android/compose/` in Android Studio
2. Sync Gradle
3. Run on device or emulator

The Compose project has the full architecture scaffolded (navigation, theme, data models) with TODO markers for wiring up each screen's content. This is the recommended path for long-term development.

---

## Data Format

Both web and Android apps use the same JSON schema, making import/export seamless between platforms:

```json
{
  "books": [
    {
      "id": "unique-id",
      "title": "Atomic Habits",
      "author": "James Clear",
      "category": "Self-Awareness",
      "status": "completed",
      "pages": 320,
      "currentPage": 320,
      "rating": 5,
      "startDate": "2025-01-05",
      "endDate": "2025-01-18",
      "notes": "...",
      "themes": ["habits", "systems"],
      "quotes": [{"text": "...", "page": "27"}],
      "sessions": [{"date": "2025-01-08", "duration": 60, "pagesRead": 100}]
    }
  ],
  "settings": {
    "goal": 50,
    "goals": { "annual": 50, "categories": {}, "challenges": [] }
  }
}
```

---

## License

Personal project by Malcolm Bramble.
