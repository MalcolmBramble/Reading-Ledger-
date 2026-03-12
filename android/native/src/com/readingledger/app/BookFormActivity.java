package com.readingledger.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.readingledger.app.db.DataStore;
import com.readingledger.app.model.Book;
import com.readingledger.app.util.C;
import java.util.Calendar;

public class BookFormActivity extends Activity {
    private DataStore ds;
    private Book book;
    private boolean isEdit;
    private ScrollView scrollView;
    private LinearLayout content;

    private EditText fTitle, fAuthor, fPages, fNotes, fCoreArg, fImpact, fRecommendedBy;
    private Spinner fCategory, fStatus;
    private TextView fStart, fEnd;
    private String startDate = "", endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ds = DataStore.get(this);

        String bookId = getIntent().getStringExtra("bookId");
        if (bookId != null) {
            book = ds.getBook(bookId);
            isEdit = true;
        }
        if (book == null) {
            book = new Book();
            isEdit = false;
        }

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

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(isEdit ? "Edit Book" : "Add Book");
        title.setTextColor(C.TEXT);
        title.setTextSize(22);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(C.TEXT_M);
        close.setTextSize(20);
        close.setPadding(dp(12), dp(8), dp(4), dp(8));
        close.setOnClickListener(v -> finish());
        header.addView(close);

        content.addView(header);
        addSpacer(16);

        // Title field
        content.addView(fieldLabel("Title *"));
        fTitle = editField("Book title");
        if (isEdit) fTitle.setText(book.title);
        content.addView(fTitle);

        // Author
        addSpacer(12);
        content.addView(fieldLabel("Author"));
        fAuthor = editField("Author name");
        if (isEdit && book.author != null) fAuthor.setText(book.author);
        content.addView(fAuthor);

        // Category
        addSpacer(12);
        content.addView(fieldLabel("Category"));
        fCategory = new Spinner(this);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, C.CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fCategory.setAdapter(catAdapter);
        if (isEdit) {
            for (int i = 0; i < C.CATEGORIES.length; i++) {
                if (C.CATEGORIES[i].equals(book.category)) { fCategory.setSelection(i); break; }
            }
        }
        GradientDrawable spinBg = new GradientDrawable();
        spinBg.setColor(C.SURFACE);
        spinBg.setCornerRadius(dp(8));
        spinBg.setStroke(dp(1), C.BORDER);
        fCategory.setBackground(spinBg);
        fCategory.setPadding(dp(8), dp(4), dp(8), dp(4));
        content.addView(fCategory, new LinearLayout.LayoutParams(-1, dp(44)));

        // Status
        addSpacer(12);
        content.addView(fieldLabel("Status"));
        fStatus = new Spinner(this);
        String[] statusLabels = {"To Read", "Reading", "Completed", "Dropped"};
        String[] statusValues = {C.S_WANT, C.S_READING, C.S_DONE, C.S_DROPPED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusLabels);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fStatus.setAdapter(statusAdapter);
        if (isEdit) {
            for (int i = 0; i < statusValues.length; i++) {
                if (statusValues[i].equals(book.status)) { fStatus.setSelection(i); break; }
            }
        } else {
            // Smart default: if no books reading, default to reading
            fStatus.setSelection(ds.getReading().isEmpty() ? 1 : 0);
        }
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(C.SURFACE);
        sBg.setCornerRadius(dp(8));
        sBg.setStroke(dp(1), C.BORDER);
        fStatus.setBackground(sBg);
        fStatus.setPadding(dp(8), dp(4), dp(8), dp(4));
        content.addView(fStatus, new LinearLayout.LayoutParams(-1, dp(44)));

        // Pages
        addSpacer(12);
        content.addView(fieldLabel("Pages"));
        fPages = editField("Number of pages");
        fPages.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (isEdit && book.pages > 0) fPages.setText(String.valueOf(book.pages));
        content.addView(fPages);

        // Dates
        addSpacer(12);
        LinearLayout dateRow = new LinearLayout(this);
        dateRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout startCol = new LinearLayout(this);
        startCol.setOrientation(LinearLayout.VERTICAL);
        startCol.addView(fieldLabel("Started"));
        startDate = isEdit && book.startDate != null ? book.startDate : "";
        fStart = dateField(startDate);
        fStart.setOnClickListener(v -> pickDate(true));
        startCol.addView(fStart);
        dateRow.addView(startCol, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout endCol = new LinearLayout(this);
        endCol.setOrientation(LinearLayout.VERTICAL);
        endCol.setPadding(dp(8), 0, 0, 0);
        endCol.addView(fieldLabel("Finished"));
        endDate = isEdit && book.endDate != null ? book.endDate : "";
        fEnd = dateField(endDate);
        fEnd.setOnClickListener(v -> pickDate(false));
        endCol.addView(fEnd);
        dateRow.addView(endCol, new LinearLayout.LayoutParams(0, -2, 1));

        content.addView(dateRow);

        // Notes
        addSpacer(12);
        content.addView(fieldLabel("Notes"));
        fNotes = editField("Your notes...");
        fNotes.setMinLines(3);
        fNotes.setSingleLine(false);
        if (isEdit && book.notes != null) fNotes.setText(book.notes);
        content.addView(fNotes);

        // Core Argument
        addSpacer(12);
        content.addView(fieldLabel("Core Argument"));
        fCoreArg = editField("What is the book's thesis?");
        fCoreArg.setMinLines(2);
        fCoreArg.setSingleLine(false);
        if (isEdit && book.coreArgument != null) fCoreArg.setText(book.coreArgument);
        content.addView(fCoreArg);

        // Impact
        addSpacer(12);
        content.addView(fieldLabel("Impact"));
        fImpact = editField("How did this book affect you?");
        fImpact.setMinLines(2);
        fImpact.setSingleLine(false);
        if (isEdit && book.impact != null) fImpact.setText(book.impact);
        content.addView(fImpact);

        // Recommended By
        addSpacer(12);
        content.addView(fieldLabel("Recommended By"));
        fRecommendedBy = editField("Who recommended this?");
        if (isEdit && book.recommendedBy != null) fRecommendedBy.setText(book.recommendedBy);
        content.addView(fRecommendedBy);

        // Save button
        addSpacer(24);
        Button save = new Button(this);
        save.setText(isEdit ? "Save Changes" : "Add to Library");
        save.setTextColor(C.BG);
        save.setTextSize(15);
        save.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(C.ACCENT);
        saveBg.setCornerRadius(dp(10));
        save.setBackground(saveBg);
        save.setPadding(dp(24), dp(14), dp(24), dp(14));
        save.setAllCaps(false);
        save.setOnClickListener(v -> saveBook());
        content.addView(save, new LinearLayout.LayoutParams(-1, -2));
    }

    private void saveBook() {
        String title = fTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] statusValues = {C.S_WANT, C.S_READING, C.S_DONE, C.S_DROPPED};
        String now = C.isoNow();

        book.title = title;
        book.author = fAuthor.getText().toString().trim();
        book.category = C.CATEGORIES[fCategory.getSelectedItemPosition()];
        book.status = statusValues[fStatus.getSelectedItemPosition()];
        book.pages = parseInt(fPages.getText().toString());
        book.startDate = startDate.isEmpty() ? null : startDate;
        book.endDate = endDate.isEmpty() ? null : endDate;
        book.notes = fNotes.getText().toString().trim();
        book.coreArgument = fCoreArg.getText().toString().trim();
        book.impact = fImpact.getText().toString().trim();
        book.recommendedBy = fRecommendedBy.getText().toString().trim();
        book.updatedAt = now;

        // Smart date auto-fill
        if ("reading".equals(book.status) && (book.startDate == null || book.startDate.isEmpty())) {
            book.startDate = C.today();
        }
        if ("completed".equals(book.status)) {
            if (book.startDate == null || book.startDate.isEmpty()) book.startDate = C.today();
            if (book.endDate == null || book.endDate.isEmpty()) book.endDate = C.today();
            book.currentPage = book.pages;
        }

        if (isEdit) {
            ds.updateBook(book);
        } else {
            book.id = C.uid();
            book.addedAt = now;
            ds.addBook(book);
        }

        finish();
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        String current = isStart ? startDate : endDate;
        if (current != null && !current.isEmpty()) {
            try {
                String[] parts = current.split("-");
                cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception e) {}
        }
        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format("%04d-%02d-%02d", y, m + 1, d);
            if (isStart) { startDate = date; fStart.setText(C.fmtDate(date)); }
            else { endDate = date; fEnd.setText(C.fmtDate(date)); }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private TextView fieldLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C.TEXT_M);
        tv.setTextSize(12);
        tv.setPadding(0, 0, 0, dp(4));
        return tv;
    }

    private EditText editField(String hint) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setHintTextColor(C.TEXT_D);
        et.setTextColor(C.TEXT);
        et.setTextSize(14);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), C.BORDER);
        et.setBackground(bg);
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        return et;
    }

    private TextView dateField(String value) {
        TextView tv = new TextView(this);
        tv.setText(value.isEmpty() ? "Pick date" : C.fmtDate(value));
        tv.setTextColor(value.isEmpty() ? C.TEXT_D : C.TEXT);
        tv.setTextSize(14);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), C.BORDER);
        tv.setBackground(bg);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        tv.setClickable(true);
        return tv;
    }

    private int dp(int dp) { return C.dp(this, dp); }

    private void addSpacer(int h) {
        View s = new View(this);
        content.addView(s, new LinearLayout.LayoutParams(-1, dp(h)));
    }
}
