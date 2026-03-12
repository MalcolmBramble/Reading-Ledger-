package com.readingledger.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.readingledger.app.db.DataStore;
import com.readingledger.app.model.Book;
import com.readingledger.app.util.C;
import java.util.ArrayList;

public class BookDetailActivity extends Activity {
    private DataStore ds;
    private Book book;
    private ScrollView scrollView;
    private LinearLayout content;

    // Timer state
    private boolean timerRunning = false;
    private long timerStart = 0;
    private long timerElapsed = 0;
    private Handler timerHandler = new Handler();
    private TextView timerDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ds = DataStore.get(this);

        String bookId = getIntent().getStringExtra("bookId");
        book = ds.getBook(bookId);
        if (book == null) { finish(); return; }

        getWindow().setStatusBarColor(C.BG);
        getWindow().setNavigationBarColor(C.BG);

        buildUI();
    }

    private void buildUI() {
        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(C.BG);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(44), dp(20), dp(40));
        scrollView.addView(content, new FrameLayout.LayoutParams(-1, -2));
        setContentView(scrollView);

        int catColor = C.catColor(book.category);

        // Back button + actions row
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = new TextView(this);
        back.setText("← Back");
        back.setTextColor(C.ACCENT);
        back.setTextSize(14);
        back.setOnClickListener(v -> finish());
        topRow.addView(back, new LinearLayout.LayoutParams(0, -2, 1));

        TextView editBtn = new TextView(this);
        editBtn.setText("Edit");
        editBtn.setTextColor(C.ACCENT);
        editBtn.setTextSize(14);
        editBtn.setPadding(dp(12), dp(8), dp(12), dp(8));
        editBtn.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(this, BookFormActivity.class);
            i.putExtra("bookId", book.id);
            startActivity(i);
        });
        topRow.addView(editBtn);

        TextView deleteBtn = new TextView(this);
        deleteBtn.setText("Delete");
        deleteBtn.setTextColor(C.RED);
        deleteBtn.setTextSize(14);
        deleteBtn.setPadding(dp(12), dp(8), 0, dp(8));
        deleteBtn.setOnClickListener(v -> confirmDelete());
        topRow.addView(deleteBtn);

        content.addView(topRow, new LinearLayout.LayoutParams(-1, -2));
        addSpacer(12);

        // Category + Status
        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.addView(badge(book.category, catColor));
        badges.addView(badge(C.statusLabel(book.status), statusColor(book.status)));
        content.addView(badges);
        addSpacer(8);

        // Title
        TextView title = new TextView(this);
        title.setText(book.title);
        title.setTextColor(C.TEXT);
        title.setTextSize(24);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        content.addView(title);

        // Author
        if (book.author != null && !book.author.isEmpty()) {
            TextView author = new TextView(this);
            author.setText("by " + book.author);
            author.setTextColor(C.TEXT_M);
            author.setTextSize(14);
            author.setTypeface(Typeface.SERIF, Typeface.ITALIC);
            author.setPadding(0, dp(2), 0, 0);
            content.addView(author);
        }

        // Rating
        if (book.rating > 0) {
            addSpacer(8);
            LinearLayout ratingRow = new LinearLayout(this);
            ratingRow.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 1; i <= 5; i++) {
                TextView star = new TextView(this);
                star.setText(i <= book.rating ? "★" : "☆");
                star.setTextColor(i <= book.rating ? C.GOLD : C.TEXT_D);
                star.setTextSize(20);
                final int rating = i;
                star.setOnClickListener(v -> {
                    book.rating = rating;
                    book.updatedAt = C.isoNow();
                    ds.save();
                    rebuildUI();
                });
                ratingRow.addView(star);
            }
            content.addView(ratingRow);
        } else {
            // Tap to rate
            addSpacer(8);
            LinearLayout ratingRow = new LinearLayout(this);
            ratingRow.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 1; i <= 5; i++) {
                TextView star = new TextView(this);
                star.setText("☆");
                star.setTextColor(C.TEXT_D);
                star.setTextSize(20);
                final int rating = i;
                star.setOnClickListener(v -> {
                    book.rating = rating;
                    book.updatedAt = C.isoNow();
                    ds.save();
                    rebuildUI();
                });
                ratingRow.addView(star);
            }
            content.addView(ratingRow);
        }

        // Meta info
        addSpacer(12);
        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.VERTICAL);
        if (book.pages > 0) addMeta(meta, "Pages", String.valueOf(book.pages));
        if (book.startDate != null && !book.startDate.isEmpty()) addMeta(meta, "Started", C.fmtDate(book.startDate));
        if (book.endDate != null && !book.endDate.isEmpty()) addMeta(meta, "Finished", C.fmtDate(book.endDate));
        if (book.recommendedBy != null && !book.recommendedBy.isEmpty()) addMeta(meta, "Recommended by", book.recommendedBy);
        content.addView(meta);

        // Progress (if reading)
        if ("reading".equals(book.status) && book.pages > 0) {
            addSpacer(16);
            content.addView(sectionTitle("Progress"));

            int pct = book.getProgressPercent();
            TextView pctText = new TextView(this);
            pctText.setText(book.currentPage + " / " + book.pages + " pages (" + pct + "%)");
            pctText.setTextColor(C.TEXT_M);
            pctText.setTextSize(13);
            content.addView(pctText);

            FrameLayout bar = new FrameLayout(this);
            GradientDrawable barBg = new GradientDrawable();
            barBg.setColor(C.SURFACE2);
            barBg.setCornerRadius(dp(4));
            bar.setBackground(barBg);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, dp(8));
            blp.setMargins(0, dp(6), 0, 0);
            bar.setLayoutParams(blp);

            View fill = new View(this);
            GradientDrawable fillBg = new GradientDrawable();
            fillBg.setColor(catColor);
            fillBg.setCornerRadius(dp(4));
            fill.setBackground(fillBg);
            int screenW = getWindowManager().getDefaultDisplay().getWidth() - dp(40);
            bar.addView(fill, new FrameLayout.LayoutParams((int)(screenW * pct / 100f), dp(8)));
            content.addView(bar);

            // Reading timer
            addSpacer(12);
            content.addView(buildTimer());
        }

        // Notes
        if (book.notes != null && !book.notes.isEmpty()) {
            addSpacer(16);
            content.addView(sectionTitle("Notes"));
            TextView notes = new TextView(this);
            notes.setText(book.notes);
            notes.setTextColor(C.TEXT_M);
            notes.setTextSize(13);
            notes.setLineSpacing(dp(3), 1);
            content.addView(notes);
        }

        // Core Argument
        if (book.coreArgument != null && !book.coreArgument.isEmpty()) {
            addSpacer(16);
            content.addView(sectionTitle("Core Argument"));
            TextView ca = new TextView(this);
            ca.setText(book.coreArgument);
            ca.setTextColor(C.TEXT_M);
            ca.setTextSize(13);
            ca.setTypeface(Typeface.SERIF, Typeface.ITALIC);
            ca.setLineSpacing(dp(3), 1);
            content.addView(ca);
        }

        // Impact
        if (book.impact != null && !book.impact.isEmpty()) {
            addSpacer(12);
            content.addView(sectionTitle("Impact"));
            TextView imp = new TextView(this);
            imp.setText(book.impact);
            imp.setTextColor(C.TEXT_M);
            imp.setTextSize(13);
            imp.setLineSpacing(dp(3), 1);
            content.addView(imp);
        }

        // Quotes
        if (!book.quotes.isEmpty()) {
            addSpacer(16);
            content.addView(sectionTitle("Quotes (" + book.quotes.size() + ")"));
            for (Book.Quote q : book.quotes) {
                content.addView(buildQuoteCard(q));
            }
        }

        // Add quote button
        addSpacer(8);
        Button addQuote = styledButtonSecondary("+ Add Quote");
        addQuote.setOnClickListener(v -> showAddQuoteDialog());
        content.addView(addQuote);

        // Themes
        if (!book.themes.isEmpty()) {
            addSpacer(16);
            content.addView(sectionTitle("Themes"));
            LinearLayout themesRow = new LinearLayout(this);
            themesRow.setOrientation(LinearLayout.HORIZONTAL);
            for (String t : book.themes) {
                themesRow.addView(badge(t, C.SURFACE2));
            }
            content.addView(themesRow);
        }

        // Sessions
        if (!book.sessions.isEmpty()) {
            addSpacer(16);
            content.addView(sectionTitle("Reading Sessions (" + book.sessions.size() + ")"));
            for (Book.Session s : book.sessions) {
                LinearLayout sRow = new LinearLayout(this);
                sRow.setOrientation(LinearLayout.HORIZONTAL);
                sRow.setPadding(0, dp(4), 0, dp(4));

                TextView date = new TextView(this);
                date.setText(C.fmtShort(s.date));
                date.setTextColor(C.TEXT_D);
                date.setTextSize(12);
                date.setMinWidth(dp(60));
                sRow.addView(date);

                TextView detail = new TextView(this);
                detail.setText(s.pagesRead + " pages · " + s.duration + " min");
                detail.setTextColor(C.TEXT_M);
                detail.setTextSize(12);
                sRow.addView(detail);

                content.addView(sRow);
            }
        }
    }

    private void rebuildUI() {
        content.removeAllViews();
        buildUI();
    }

    private View buildTimer() {
        LinearLayout timer = new LinearLayout(this);
        timer.setOrientation(LinearLayout.VERTICAL);
        timer.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(12));
        timer.setBackground(bg);
        timer.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView label = new TextView(this);
        label.setText("Reading Timer");
        label.setTextColor(C.TEXT_M);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        timer.addView(label);

        timerDisplay = new TextView(this);
        timerDisplay.setText("0:00");
        timerDisplay.setTextColor(C.TEXT);
        timerDisplay.setTextSize(28);
        timerDisplay.setTypeface(Typeface.MONOSPACE);
        timerDisplay.setGravity(Gravity.CENTER);
        timer.addView(timerDisplay);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setGravity(Gravity.CENTER);
        btns.setPadding(0, dp(8), 0, 0);

        Button startBtn = styledButtonSecondary(timerRunning ? "Pause" : "Start");
        startBtn.setOnClickListener(v -> {
            if (timerRunning) {
                timerRunning = false;
                timerElapsed += System.currentTimeMillis() - timerStart;
                startBtn.setText("Resume");
            } else {
                timerRunning = true;
                timerStart = System.currentTimeMillis();
                startBtn.setText("Pause");
                tickTimer();
            }
        });
        btns.addView(startBtn);

        Button saveBtn = styledButtonSecondary("Save Session");
        saveBtn.setOnClickListener(v -> {
            if (timerRunning) {
                timerElapsed += System.currentTimeMillis() - timerStart;
                timerRunning = false;
            }
            int minutes = (int)(timerElapsed / 60000);
            if (minutes < 1) minutes = 1;
            Book.Session s = new Book.Session();
            s.date = C.today();
            s.duration = minutes;
            s.pagesRead = 0;
            s.note = "";
            book.sessions.add(s);
            book.updatedAt = C.isoNow();
            ds.save();
            timerElapsed = 0;
            rebuildUI();
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-2, -2);
        slp.setMargins(dp(8), 0, 0, 0);
        btns.addView(saveBtn, slp);

        timer.addView(btns);
        return timer;
    }

    private void tickTimer() {
        if (!timerRunning) return;
        long elapsed = timerElapsed + (System.currentTimeMillis() - timerStart);
        int sec = (int)(elapsed / 1000);
        int min = sec / 60;
        sec = sec % 60;
        if (timerDisplay != null) timerDisplay.setText(min + ":" + String.format("%02d", sec));
        timerHandler.postDelayed(this::tickTimer, 1000);
    }

    private View buildQuoteCard(Book.Quote q) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        card.setBackground(bg);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(4), 0, dp(4));
        card.setLayoutParams(lp);

        TextView mark = new TextView(this);
        mark.setText("\u201C");
        mark.setTextColor(C.ACCENT_DIM);
        mark.setTextSize(24);
        mark.setPadding(0, 0, dp(8), 0);
        card.addView(mark, new LinearLayout.LayoutParams(-2, -2));

        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        TextView qt = new TextView(this);
        qt.setText(q.text);
        qt.setTextColor(C.TEXT);
        qt.setTextSize(13);
        qt.setTypeface(Typeface.SERIF, Typeface.ITALIC);
        qt.setLineSpacing(dp(2), 1);
        txt.addView(qt, new LinearLayout.LayoutParams(-1, -2));

        if (q.page != null && !q.page.isEmpty()) {
            TextView pg = new TextView(this);
            pg.setText("p. " + q.page);
            pg.setTextColor(C.TEXT_D);
            pg.setTextSize(11);
            pg.setPadding(0, dp(4), 0, 0);
            txt.addView(pg);
        }
        card.addView(txt, new LinearLayout.LayoutParams(0, -2, 1));

        return card;
    }

    private void showAddQuoteDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(16), dp(20), dp(8));

        EditText quoteInput = new EditText(this);
        quoteInput.setHint("Quote text");
        quoteInput.setTextColor(C.TEXT);
        quoteInput.setHintTextColor(C.TEXT_D);
        quoteInput.setMinLines(3);
        form.addView(quoteInput, new LinearLayout.LayoutParams(-1, -2));

        EditText pageInput = new EditText(this);
        pageInput.setHint("Page number (optional)");
        pageInput.setTextColor(C.TEXT);
        pageInput.setHintTextColor(C.TEXT_D);
        pageInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pageInput.setPadding(0, dp(8), 0, 0);
        form.addView(pageInput, new LinearLayout.LayoutParams(-1, -2));

        new AlertDialog.Builder(this)
            .setTitle("Add Quote")
            .setView(form)
            .setPositiveButton("Add", (d, w) -> {
                String text = quoteInput.getText().toString().trim();
                if (text.isEmpty()) return;
                Book.Quote q = new Book.Quote();
                q.text = text;
                q.page = pageInput.getText().toString().trim();
                q.note = "";
                book.quotes.add(q);
                book.updatedAt = C.isoNow();
                ds.save();
                rebuildUI();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Book")
            .setMessage("Remove \"" + book.title + "\" from your library?")
            .setPositiveButton("Delete", (d, w) -> {
                ds.deleteBook(book.id);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addMeta(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, dp(2));

        TextView l = new TextView(this);
        l.setText(label + ": ");
        l.setTextColor(C.TEXT_D);
        l.setTextSize(12);
        row.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(C.TEXT_M);
        v.setTextSize(12);
        row.addView(v);

        parent.addView(row);
    }

    private int statusColor(String s) {
        if ("completed".equals(s)) return C.GREEN;
        if ("reading".equals(s)) return C.BLUE;
        if ("abandoned".equals(s)) return C.RED;
        return C.TEXT_D;
    }

    private View badge(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color == C.SURFACE2 ? C.TEXT_M : 0xFFFFFFFF);
        tv.setTextSize(11);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor((color & 0x00FFFFFF) | 0x30000000);
        bg.setCornerRadius(dp(12));
        tv.setBackground(bg);
        tv.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, 0, dp(6), 0);
        tv.setLayoutParams(lp);
        return tv;
    }

    private int dp(int dp) { return C.dp(this, dp); }

    private void addSpacer(int h) {
        View s = new View(this);
        content.addView(s, new LinearLayout.LayoutParams(-1, dp(h)));
    }

    private TextView sectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C.TEXT_M);
        tv.setTextSize(13);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setLetterSpacing(0.05f);
        return tv;
    }

    private Button styledButtonSecondary(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(C.ACCENT);
        btn.setTextSize(12);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), C.BORDER);
        btn.setBackground(bg);
        btn.setPadding(dp(16), dp(8), dp(16), dp(8));
        btn.setAllCaps(false);
        return btn;
    }

    @Override
    protected void onResume() {
        super.onResume();
        book = ds.getBook(book.id);
        if (book == null) { finish(); return; }
        rebuildUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerRunning = false;
    }
}
