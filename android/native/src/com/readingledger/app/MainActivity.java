package com.readingledger.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.readingledger.app.db.DataStore;
import com.readingledger.app.model.Book;
import com.readingledger.app.util.C;
import java.util.*;

public class MainActivity extends Activity {
    private DataStore ds;
    private String currentTab = "shelf";
    private String currentSort = "date";
    private String searchQuery = "";
    private boolean searchOpen = false;

    private LinearLayout rootLayout;
    private FrameLayout contentFrame;
    private ScrollView contentScroll;
    private LinearLayout contentInner;
    private LinearLayout bottomNav;
    private LinearLayout searchBar;
    private EditText searchInput;

    // Tab buttons
    private LinearLayout tabShelf, tabTimeline, tabAnalytics, tabReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ds = DataStore.get(this);

        // Edge-to-edge
        Window w = getWindow();
        w.setStatusBarColor(C.BG);
        w.setNavigationBarColor(C.BG);

        buildLayout();
        setContentView(rootLayout);

        // Check if first run
        if (ds.getBooks().isEmpty()) {
            showOnboarding();
        } else {
            renderAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderAll();
    }

    // ═══════════════════════════════════════════
    //  LAYOUT CONSTRUCTION
    // ═══════════════════════════════════════════
    private void buildLayout() {
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(C.BG);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        // Header
        LinearLayout header = buildHeader();
        rootLayout.addView(header);

        // Search bar (hidden by default)
        searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setBackgroundColor(C.SURFACE);
        searchBar.setPadding(dp(16), dp(8), dp(16), dp(8));
        searchBar.setVisibility(View.GONE);
        searchInput = new EditText(this);
        searchInput.setHint("Search books...");
        searchInput.setHintTextColor(C.TEXT_D);
        searchInput.setTextColor(C.TEXT);
        searchInput.setTextSize(14);
        searchInput.setBackgroundColor(C.SURFACE2);
        searchInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        searchInput.setSingleLine(true);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(C.SURFACE2);
        searchBg.setCornerRadius(dp(8));
        searchInput.setBackground(searchBg);
        searchInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString();
                renderShelf();
            }
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
        });
        searchBar.addView(searchInput, new LinearLayout.LayoutParams(0, -2, 1));
        rootLayout.addView(searchBar, new LinearLayout.LayoutParams(-1, -2));

        // Scrollable content
        contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);
        contentScroll.setBackgroundColor(C.BG);
        contentInner = new LinearLayout(this);
        contentInner.setOrientation(LinearLayout.VERTICAL);
        contentInner.setPadding(dp(20), 0, dp(20), dp(80));
        contentScroll.addView(contentInner, new FrameLayout.LayoutParams(-1, -2));
        rootLayout.addView(contentScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        // Bottom nav
        bottomNav = buildBottomNav();
        rootLayout.addView(bottomNav);
    }

    private LinearLayout buildHeader() {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.VERTICAL);
        h.setBackgroundColor(C.BG);
        h.setPadding(dp(20), dp(44), dp(20), dp(12));

        // Top row: label + icons
        RelativeLayout topRow = new RelativeLayout(this);
        TextView label = new TextView(this);
        label.setText("PERSONAL LIBRARY");
        label.setTextColor(C.ACCENT_DIM);
        label.setTextSize(11);
        label.setLetterSpacing(0.1f);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        topRow.addView(label, lp);

        // Search icon
        TextView searchBtn = new TextView(this);
        searchBtn.setText("⌕");
        searchBtn.setTextSize(20);
        searchBtn.setTextColor(C.TEXT_M);
        searchBtn.setGravity(Gravity.CENTER);
        searchBtn.setOnClickListener(v -> toggleSearch());
        RelativeLayout.LayoutParams sip = new RelativeLayout.LayoutParams(dp(36), dp(36));
        sip.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        sip.setMargins(0, -dp(6), dp(40), 0);
        topRow.addView(searchBtn, sip);

        // Settings icon
        TextView settingsBtn = new TextView(this);
        settingsBtn.setText("⚙");
        settingsBtn.setTextSize(20);
        settingsBtn.setTextColor(C.TEXT_M);
        settingsBtn.setGravity(Gravity.CENTER);
        settingsBtn.setOnClickListener(v -> openSettings());
        RelativeLayout.LayoutParams gp = new RelativeLayout.LayoutParams(dp(36), dp(36));
        gp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        gp.setMargins(0, -dp(6), 0, 0);
        topRow.addView(settingsBtn, gp);

        h.addView(topRow, new LinearLayout.LayoutParams(-1, -2));

        // Title
        TextView title = new TextView(this);
        title.setText("The Reading Ledger");
        title.setTextColor(C.TEXT);
        title.setTextSize(28);
        title.setTypeface(Typeface.SERIF);
        title.setPadding(0, dp(2), 0, dp(8));
        h.addView(title, new LinearLayout.LayoutParams(-1, -2));

        // Goal progress pill
        LinearLayout pill = buildGoalPill();
        h.addView(pill, new LinearLayout.LayoutParams(-2, -2));

        return h;
    }

    private LinearLayout buildGoalPill() {
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), C.BORDER);
        pill.setBackground(bg);
        pill.setPadding(dp(10), dp(6), dp(14), dp(6));
        pill.setTag("goalPill");

        // Mini circle
        View circle = new View(this) {
            @Override
            protected void onDraw(Canvas c) {
                int completed = ds.getCompleted().size();
                int goal = ds.getAnnualGoal();
                float pct = Math.min(1f, (float) completed / goal);
                float r = getWidth() / 2f;
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(2));
                p.setColor(C.BORDER);
                c.drawCircle(r, r, r - dp(2), p);
                p.setColor(C.ACCENT);
                float sweep = pct * 360;
                RectF arc = new RectF(dp(2), dp(2), getWidth() - dp(2), getHeight() - dp(2));
                c.drawArc(arc, -90, sweep, false, p);
            }
        };
        circle.setWillNotDraw(false);
        pill.addView(circle, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView goalText = new TextView(this);
        goalText.setTag("goalText");
        int completed = ds.getCompleted().size();
        goalText.setText(completed + "/" + ds.getAnnualGoal());
        goalText.setTextColor(C.TEXT_M);
        goalText.setTextSize(13);
        goalText.setPadding(dp(8), 0, 0, 0);
        pill.addView(goalText, new LinearLayout.LayoutParams(-2, -2));

        return pill;
    }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(C.NAV_BG);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(8), 0, dp(8));

        // Top border
        GradientDrawable navBg = new GradientDrawable();
        navBg.setColor(C.NAV_BG);
        nav.setBackground(navBg);

        // Divider line at top
        View divider = new View(this);
        divider.setBackgroundColor(C.BORDER);

        LinearLayout navWrap = new LinearLayout(this);
        navWrap.setOrientation(LinearLayout.VERTICAL);
        navWrap.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        navWrap.addView(nav, new LinearLayout.LayoutParams(-1, dp(C.NAV_H)));

        // Create tab buttons
        tabShelf = makeNavBtn("Shelf", "\u229E", "shelf");
        tabTimeline = makeNavBtn("Timeline", "\u2261", "timeline");
        tabAnalytics = makeNavBtn("Analytics", "\u2225", "analytics");
        tabReview = makeNavBtn("Review", "\u2606", "review");

        nav.addView(tabShelf, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(tabTimeline, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(tabAnalytics, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(tabReview, new LinearLayout.LayoutParams(0, -1, 1));

        updateNavHighlight();
        return navWrap;
    }

    private LinearLayout makeNavBtn(String label, String icon, String tab) {
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.VERTICAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(4), dp(6), dp(4), dp(6));
        btn.setClickable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        btn.setBackground(bg);
        btn.setTag("tab_" + tab);

        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(20);
        ic.setGravity(Gravity.CENTER);
        ic.setTag("tabIcon");
        btn.addView(ic, new LinearLayout.LayoutParams(-2, -2));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(11);
        lbl.setGravity(Gravity.CENTER);
        lbl.setTag("tabLabel");
        btn.addView(lbl, new LinearLayout.LayoutParams(-2, -2));

        btn.setOnClickListener(v -> switchTab(tab));
        return btn;
    }

    private void updateNavHighlight() {
        LinearLayout[] tabs = {tabShelf, tabTimeline, tabAnalytics, tabReview};
        String[] names = {"shelf", "timeline", "analytics", "review"};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = names[i].equals(currentTab);
            TextView ic = (TextView) tabs[i].findViewWithTag("tabIcon");
            TextView lbl = (TextView) tabs[i].findViewWithTag("tabLabel");
            if (ic != null) ic.setTextColor(active ? C.ACCENT : C.TEXT_D);
            if (lbl != null) lbl.setTextColor(active ? C.ACCENT : C.TEXT_D);
            GradientDrawable bg = (GradientDrawable) tabs[i].getBackground();
            bg.setColor(active ? 0x20A8B88E : 0x00000000);
        }
    }

    // ═══════════════════════════════════════════
    //  ICON HELPER (text-based icons since no drawable resources)
    // ═══════════════════════════════════════════
    private TextView iconBtn(String symbol, int sizeSp) {
        TextView tv = new TextView(this);
        tv.setText(symbol);
        tv.setTextSize(sizeSp);
        tv.setTextColor(C.TEXT_M);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(8), dp(8), dp(8), dp(8));
        tv.setClickable(true);
        return tv;
    }

    // ═══════════════════════════════════════════
    //  TAB SWITCHING
    // ═══════════════════════════════════════════
    private void switchTab(String tab) {
        if (tab.equals(currentTab)) return;
        currentTab = tab;
        updateNavHighlight();
        renderAll();
    }

    private void toggleSearch() {
        searchOpen = !searchOpen;
        searchBar.setVisibility(searchOpen ? View.VISIBLE : View.GONE);
        if (searchOpen) {
            searchInput.requestFocus();
        } else {
            searchQuery = "";
            searchInput.setText("");
            renderShelf();
        }
    }

    // ═══════════════════════════════════════════
    //  RENDER ALL
    // ═══════════════════════════════════════════
    private void renderAll() {
        contentInner.removeAllViews();
        switch (currentTab) {
            case "shelf": renderShelf(); break;
            case "timeline": renderTimeline(); break;
            case "analytics": renderAnalytics(); break;
            case "review": renderReview(); break;
        }
    }

    // ═══════════════════════════════════════════
    //  SHELF TAB
    // ═══════════════════════════════════════════
    private void renderShelf() {
        contentInner.removeAllViews();
        List<Book> books = searchQuery.isEmpty() ? ds.getBooks() : ds.search(searchQuery);

        // Now Reading section
        List<Book> reading = ds.getReading();
        if (!reading.isEmpty() && searchQuery.isEmpty()) {
            TextView nrTitle = sectionTitle("Now Reading");
            contentInner.addView(nrTitle);
            for (Book b : reading) {
                contentInner.addView(buildNowReadingCard(b));
            }
            addSpacer(12);
        }

        // Sort bar
        LinearLayout sortBar = new LinearLayout(this);
        sortBar.setOrientation(LinearLayout.HORIZONTAL);
        sortBar.setGravity(Gravity.CENTER_VERTICAL);
        sortBar.setPadding(0, dp(8), 0, dp(8));

        TextView count = new TextView(this);
        count.setText(books.size() + " book" + (books.size() != 1 ? "s" : ""));
        count.setTextColor(C.TEXT_D);
        count.setTextSize(12);
        count.setPadding(0, 0, dp(12), 0);
        sortBar.addView(count, new LinearLayout.LayoutParams(-2, -2));

        HorizontalScrollView pillScroll = new HorizontalScrollView(this);
        pillScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout pills = new LinearLayout(this);
        pills.setOrientation(LinearLayout.HORIZONTAL);

        String[][] sorts = {{"date","Date"},{"rating","Rating"},{"length","Length"},{"status","Status"},{"title","Title"},{"category","Category"}};
        for (String[] s : sorts) {
            pills.addView(makeSortPill(s[0], s[1]));
        }
        pillScroll.addView(pills);
        sortBar.addView(pillScroll, new LinearLayout.LayoutParams(0, -2, 1));
        contentInner.addView(sortBar);

        if (books.isEmpty()) {
            // Empty state
            addSpacer(60);
            TextView emptyTitle = new TextView(this);
            emptyTitle.setText("Your shelves are empty.");
            emptyTitle.setTextColor(C.TEXT_M);
            emptyTitle.setTextSize(20);
            emptyTitle.setTypeface(Typeface.SERIF, Typeface.ITALIC);
            emptyTitle.setGravity(Gravity.CENTER);
            contentInner.addView(emptyTitle, new LinearLayout.LayoutParams(-1, -2));

            TextView emptySub = new TextView(this);
            emptySub.setText("Add your first book to begin.");
            emptySub.setTextColor(C.TEXT_D);
            emptySub.setTextSize(14);
            emptySub.setGravity(Gravity.CENTER);
            emptySub.setPadding(0, dp(8), 0, dp(20));
            contentInner.addView(emptySub, new LinearLayout.LayoutParams(-1, -2));

            Button addBtn = styledButton("+ Add a Book");
            addBtn.setOnClickListener(v -> openAddForm());
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(-2, -2);
            abp.gravity = Gravity.CENTER;
            contentInner.addView(addBtn, abp);
        } else {
            List<Book> sorted = ds.sorted(books, currentSort);
            // Render spine rows (6 per row)
            for (int i = 0; i < sorted.size(); i += 6) {
                List<Book> row = sorted.subList(i, Math.min(i + 6, sorted.size()));
                contentInner.addView(buildSpineRow(row));
            }

            // Footer stats
            addSpacer(16);
            int comp = 0, rdng = 0, want = 0;
            for (Book b : books) {
                if ("completed".equals(b.status)) comp++;
                else if ("reading".equals(b.status)) rdng++;
                else if ("want-to-read".equals(b.status)) want++;
            }
            TextView footer = new TextView(this);
            footer.setText(books.size() + " books · " + comp + " read · " + rdng + " reading · " + want + " want");
            footer.setTextColor(C.TEXT_D);
            footer.setTextSize(12);
            footer.setGravity(Gravity.CENTER);
            footer.setPadding(0, dp(8), 0, dp(8));
            contentInner.addView(footer, new LinearLayout.LayoutParams(-1, -2));
        }

        // FAB
        addFab();
    }

    private View buildSpineRow(List<Book> row) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(8), 0, 0);

        LinearLayout spines = new LinearLayout(this);
        spines.setOrientation(LinearLayout.HORIZONTAL);
        spines.setGravity(Gravity.CENTER_HORIZONTAL);
        spines.setPadding(0, 0, 0, 0);

        for (Book b : row) {
            View spine = new View(this) {
                @Override
                protected void onDraw(Canvas c) {
                    int w = getWidth(), h = getHeight();
                    int sc = C.spineColor(b.category);
                    int lc = C.catColor(b.category);

                    // Gradient background
                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    LinearGradient grad = new LinearGradient(0, 0, w, h, sc, (lc & 0x00FFFFFF) | 0x80000000, Shader.TileMode.CLAMP);
                    p.setShader(grad);
                    RectF rect = new RectF(0, 0, w, h);
                    c.drawRoundRect(rect, dp(3), dp(3), p);

                    // Border
                    p.setShader(null);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(1);
                    p.setColor((lc & 0x00FFFFFF) | 0x25000000);
                    c.drawRoundRect(rect, dp(3), dp(3), p);

                    // Sheen
                    p.setStyle(Paint.Style.FILL);
                    LinearGradient sheen = new LinearGradient(0, 0, w, 0,
                        new int[]{0x15FFFFFF, 0x00FFFFFF, 0x08FFFFFF},
                        new float[]{0, 0.4f, 1}, Shader.TileMode.CLAMP);
                    p.setShader(sheen);
                    c.drawRoundRect(rect, dp(3), dp(3), p);

                    // Gold top for 4+ stars
                    if (b.rating >= 4) {
                        p.setShader(null);
                        p.setColor(C.GOLD);
                        c.drawRoundRect(new RectF(dp(2), 0, w - dp(2), dp(3)), dp(1), dp(1), p);
                    }

                    // Title text (vertical)
                    p.setShader(null);
                    p.setColor(0xEBFFFFFF);
                    p.setTextSize(dp(9));
                    p.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
                    String title = b.title;
                    if (title.length() > 20) title = title.substring(0, 18) + "…";

                    // Draw text vertically centered
                    c.save();
                    c.rotate(90, w / 2f, h / 2f);
                    float tw = p.measureText(title);
                    float x = w / 2f - tw / 2f;
                    float y = h / 2f + dp(3);
                    c.drawText(title, h / 2f - tw / 2f, w / 2f + dp(3), p);
                    c.restore();

                    // Reading dot
                    if ("reading".equals(b.status)) {
                        p.setColor(C.ACCENT);
                        c.drawCircle(w / 2f, h - dp(8), dp(3), p);
                    }
                    // Want-to-read mark
                    if ("want-to-read".equals(b.status)) {
                        p.setColor(C.TEXT_D);
                        p.setTextSize(dp(8));
                        c.drawText("○", w / 2f - dp(4), h - dp(4), p);
                    }
                }
            };
            spine.setWillNotDraw(false);
            spine.setOnClickListener(v -> openDetail(b.id));
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(C.SPINE_W), dp(C.SPINE_H));
            sp.setMargins(dp(3), 0, dp(3), 0);
            spines.addView(spine, sp);
        }

        wrap.addView(spines, new LinearLayout.LayoutParams(-1, -2));

        // Shelf board
        View board = new View(this);
        GradientDrawable boardBg = new GradientDrawable();
        boardBg.setColor(0xFF2A2520);
        boardBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(4), dp(4), dp(4), dp(4)});
        board.setBackground(boardBg);
        board.setElevation(dp(2));
        wrap.addView(board, new LinearLayout.LayoutParams(-1, dp(6)));

        return wrap;
    }

    private View buildNowReadingCard(Book b) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), C.BORDER);
        card.setBackground(bg);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(clp);
        card.setOnClickListener(v -> openDetail(b.id));

        int catColor = C.catColor(b.category);

        // Header row: accent bar + title/author + pct
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Accent bar
        View accent = new View(this);
        GradientDrawable abg = new GradientDrawable();
        abg.setColor(catColor);
        abg.setCornerRadius(dp(2));
        accent.setBackground(abg);
        header.addView(accent, new LinearLayout.LayoutParams(dp(3), dp(36)));

        // Title/author
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(10), 0, dp(8), 0);

        TextView title = new TextView(this);
        title.setText(b.title);
        title.setTextColor(C.TEXT);
        title.setTextSize(14);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        title.setSingleLine(true);
        info.addView(title, new LinearLayout.LayoutParams(-2, -2));

        if (b.author != null && !b.author.isEmpty()) {
            TextView author = new TextView(this);
            author.setText(b.author);
            author.setTextColor(C.TEXT_D);
            author.setTextSize(12);
            author.setSingleLine(true);
            info.addView(author, new LinearLayout.LayoutParams(-2, -2));
        }
        header.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        // Percentage
        int pct = b.getProgressPercent();
        TextView pctText = new TextView(this);
        pctText.setText(pct + "%");
        pctText.setTextColor(catColor);
        pctText.setTextSize(16);
        pctText.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(pctText, new LinearLayout.LayoutParams(-2, -2));

        card.addView(header, new LinearLayout.LayoutParams(-1, -2));

        // Progress bar
        FrameLayout bar = new FrameLayout(this);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(C.SURFACE2);
        barBg.setCornerRadius(dp(3));
        bar.setBackground(barBg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, dp(4));
        blp.setMargins(0, dp(8), 0, dp(8));
        bar.setLayoutParams(blp);

        View fill = new View(this);
        GradientDrawable fillBg = new GradientDrawable();
        fillBg.setColor(catColor);
        fillBg.setCornerRadius(dp(3));
        fill.setBackground(fillBg);
        bar.addView(fill, new FrameLayout.LayoutParams(
            (int)(getResources().getDisplayMetrics().widthPixels * 0.85f * pct / 100f), dp(4)));

        card.addView(bar);

        // Page stepper
        LinearLayout stepper = new LinearLayout(this);
        stepper.setOrientation(LinearLayout.HORIZONTAL);
        stepper.setGravity(Gravity.CENTER);

        String[] increments = {"-10", "-1", null, "+1", "+10"};
        for (String inc : increments) {
            if (inc == null) {
                // Center: current page display
                TextView pg = new TextView(this);
                pg.setText(String.valueOf(b.currentPage));
                pg.setTextColor(C.TEXT);
                pg.setTextSize(16);
                pg.setTypeface(Typeface.DEFAULT_BOLD);
                pg.setGravity(Gravity.CENTER);
                pg.setPadding(dp(16), dp(4), dp(16), dp(4));
                stepper.addView(pg, new LinearLayout.LayoutParams(-2, -2));
            } else {
                Button btn = new Button(this);
                btn.setText(inc.replace("-", "−"));
                btn.setTextColor(C.TEXT_M);
                btn.setTextSize(13);
                btn.setBackgroundColor(0x00000000);
                btn.setPadding(dp(12), dp(4), dp(12), dp(4));
                GradientDrawable btnBg = new GradientDrawable();
                btnBg.setColor(C.SURFACE2);
                btnBg.setCornerRadius(dp(6));
                btn.setBackground(btnBg);
                int delta = Integer.parseInt(inc);
                btn.setOnClickListener(v -> {
                    b.currentPage = Math.max(0, Math.min(b.pages, b.currentPage + delta));
                    b.updatedAt = C.isoNow();
                    if (b.currentPage >= b.pages && "reading".equals(b.status)) {
                        b.status = "completed";
                        b.endDate = C.today();
                        b.currentPage = b.pages;
                    }
                    ds.save();
                    renderShelf();
                });
                LinearLayout.LayoutParams blp2 = new LinearLayout.LayoutParams(-2, dp(32));
                blp2.setMargins(dp(3), 0, dp(3), 0);
                stepper.addView(btn, blp2);
            }
        }
        card.addView(stepper);

        // "of X pages"
        TextView ofPages = new TextView(this);
        ofPages.setText("of " + b.pages + " pages");
        ofPages.setTextColor(C.TEXT_D);
        ofPages.setTextSize(11);
        ofPages.setGravity(Gravity.CENTER);
        ofPages.setPadding(0, dp(2), 0, 0);
        card.addView(ofPages, new LinearLayout.LayoutParams(-1, -2));

        return card;
    }

    private TextView makeSortPill(String key, String label) {
        TextView pill = new TextView(this);
        pill.setText(label);
        pill.setTextSize(12);
        pill.setPadding(dp(14), dp(6), dp(14), dp(6));
        boolean active = key.equals(currentSort);
        pill.setTextColor(active ? C.BG : C.TEXT_M);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        if (active) {
            bg.setColor(C.ACCENT);
        } else {
            bg.setColor(0x00000000);
            bg.setStroke(dp(1), C.BORDER);
        }
        pill.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(3), 0, dp(3), 0);
        pill.setLayoutParams(lp);
        pill.setOnClickListener(v -> {
            currentSort = key;
            renderShelf();
        });
        return pill;
    }

    // ═══════════════════════════════════════════
    //  TIMELINE TAB
    // ═══════════════════════════════════════════
    private void renderTimeline() {
        contentInner.removeAllViews();
        contentInner.addView(sectionTitle("Timeline"));

        // Gather all events
        List<TimelineEvent> events = new ArrayList<>();
        for (Book b : ds.getBooks()) {
            if (b.addedAt != null && !b.addedAt.isEmpty())
                events.add(new TimelineEvent(b.addedAt, "Added", b.title, b.category, "added"));
            if (b.startDate != null && !b.startDate.isEmpty())
                events.add(new TimelineEvent(b.startDate, "Started reading", b.title, b.category, "started"));
            if (b.endDate != null && !b.endDate.isEmpty()) {
                String verb = "abandoned".equals(b.status) ? "Dropped" : "Finished";
                events.add(new TimelineEvent(b.endDate, verb, b.title, b.category, "completed".equals(b.status) ? "finished" : "dropped"));
            }
            for (Book.Session s : b.sessions) {
                if (s.date != null && !s.date.isEmpty()) {
                    events.add(new TimelineEvent(s.date, "Read " + s.pagesRead + " pages (" + s.duration + " min)", b.title, b.category, "session"));
                }
            }
        }
        Collections.sort(events, (a, b) -> b.date.compareTo(a.date));

        if (events.isEmpty()) {
            addSpacer(40);
            TextView empty = new TextView(this);
            empty.setText("No activity yet.\nStart reading to see your timeline.");
            empty.setTextColor(C.TEXT_D);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            contentInner.addView(empty, new LinearLayout.LayoutParams(-1, -2));
            return;
        }

        int limit = Math.min(events.size(), 30);
        String lastMonth = "";
        for (int i = 0; i < limit; i++) {
            TimelineEvent ev = events.get(i);
            String month = ev.date.length() >= 7 ? ev.date.substring(0, 7) : "";
            if (!month.equals(lastMonth)) {
                lastMonth = month;
                TextView mh = new TextView(this);
                mh.setText(C.fmtDate(month + "-01").replaceAll("\\d+,", "").trim());
                mh.setTextColor(C.TEXT_M);
                mh.setTextSize(13);
                mh.setTypeface(Typeface.DEFAULT_BOLD);
                mh.setPadding(0, dp(16), 0, dp(4));
                contentInner.addView(mh);
            }
            contentInner.addView(buildTimelineItem(ev));
        }
    }

    private View buildTimelineItem(TimelineEvent ev) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(0, dp(8), 0, dp(8));

        // Dot
        View dot = new View(this) {
            @Override
            protected void onDraw(Canvas c) {
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                int color = C.catColor(ev.category);
                if ("finished".equals(ev.type)) color = C.GREEN;
                else if ("dropped".equals(ev.type)) color = C.TEXT_D;
                p.setColor(color);
                c.drawCircle(getWidth()/2f, getHeight()/2f, dp(4), p);
            }
        };
        dot.setWillNotDraw(false);
        item.addView(dot, new LinearLayout.LayoutParams(dp(16), dp(16)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(8), 0, 0, 0);

        TextView action = new TextView(this);
        action.setText(ev.action);
        action.setTextColor(C.TEXT_M);
        action.setTextSize(12);
        text.addView(action);

        TextView bookTitle = new TextView(this);
        bookTitle.setText(ev.bookTitle);
        bookTitle.setTextColor(C.TEXT);
        bookTitle.setTextSize(13);
        bookTitle.setTypeface(Typeface.SERIF);
        text.addView(bookTitle);

        TextView date = new TextView(this);
        date.setText(C.fmtShort(ev.date));
        date.setTextColor(C.TEXT_D);
        date.setTextSize(11);
        text.addView(date);

        item.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        return item;
    }

    static class TimelineEvent {
        String date, action, bookTitle, category, type;
        TimelineEvent(String d, String a, String bt, String cat, String t) {
            date = d; action = a; bookTitle = bt; category = cat; type = t;
        }
    }

    // ═══════════════════════════════════════════
    //  ANALYTICS TAB
    // ═══════════════════════════════════════════
    private void renderAnalytics() {
        contentInner.removeAllViews();
        contentInner.addView(sectionTitle("Analytics"));

        List<Book> completed = ds.getCompleted();
        List<Book> all = ds.getBooks();

        if (completed.isEmpty()) {
            addSpacer(40);
            TextView empty = new TextView(this);
            empty.setText("Complete at least 1 book\nto see analytics.");
            empty.setTextColor(C.TEXT_D);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            contentInner.addView(empty, new LinearLayout.LayoutParams(-1, -2));
            return;
        }

        // Stats grid
        int totalPages = ds.totalPages();
        float avgRating = 0;
        int rated = 0;
        for (Book b : completed) if (b.rating > 0) { avgRating += b.rating; rated++; }
        if (rated > 0) avgRating /= rated;

        int totalMinutes = 0;
        for (Book b : all) for (Book.Session s : b.sessions) totalMinutes += s.duration;

        LinearLayout statsGrid = new LinearLayout(this);
        statsGrid.setOrientation(LinearLayout.HORIZONTAL);
        statsGrid.addView(statCard("Books Read", String.valueOf(completed.size())), new LinearLayout.LayoutParams(0, -2, 1));
        statsGrid.addView(statCard("Pages", String.format("%,d", totalPages)), new LinearLayout.LayoutParams(0, -2, 1));
        statsGrid.addView(statCard("Avg Rating", rated > 0 ? String.format("%.1f★", avgRating) : "—"), new LinearLayout.LayoutParams(0, -2, 1));
        statsGrid.addView(statCard("Hours", String.valueOf(totalMinutes / 60)), new LinearLayout.LayoutParams(0, -2, 1));
        contentInner.addView(statsGrid);

        // Category breakdown
        addSpacer(16);
        contentInner.addView(sectionTitle("By Category"));

        Map<String, Integer> catCounts = new LinkedHashMap<>();
        for (Book b : completed) {
            catCounts.put(b.category, catCounts.getOrDefault(b.category, 0) + 1);
        }
        List<Map.Entry<String, Integer>> catList = new ArrayList<>(catCounts.entrySet());
        Collections.sort(catList, (a, b) -> b.getValue() - a.getValue());

        for (Map.Entry<String, Integer> entry : catList) {
            contentInner.addView(buildCatBar(entry.getKey(), entry.getValue(), completed.size()));
        }

        // Rating distribution
        addSpacer(16);
        contentInner.addView(sectionTitle("Ratings"));
        int[] ratingDist = new int[6];
        for (Book b : completed) if (b.rating >= 1 && b.rating <= 5) ratingDist[b.rating]++;
        for (int r = 5; r >= 1; r--) {
            contentInner.addView(buildRatingBar(r, ratingDist[r], completed.size()));
        }

        // Reading pace
        addSpacer(16);
        contentInner.addView(sectionTitle("Reading Pace"));
        Map<String, Integer> monthlyBooks = new TreeMap<>();
        for (Book b : completed) {
            if (b.endDate != null && b.endDate.length() >= 7) {
                String month = b.endDate.substring(0, 7);
                monthlyBooks.put(month, monthlyBooks.getOrDefault(month, 0) + 1);
            }
        }
        for (Map.Entry<String, Integer> entry : monthlyBooks.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView label = new TextView(this);
            label.setText(C.fmtDate(entry.getKey() + "-01").replaceAll("\\d+,", "").trim());
            label.setTextColor(C.TEXT_M);
            label.setTextSize(12);
            label.setMinWidth(dp(80));
            row.addView(label);

            View bar = new View(this);
            GradientDrawable barBg = new GradientDrawable();
            barBg.setColor(C.ACCENT);
            barBg.setCornerRadius(dp(3));
            bar.setBackground(barBg);
            int maxBooks = 1;
            for (int v : monthlyBooks.values()) maxBooks = Math.max(maxBooks, v);
            int barW = (int)(dp(180) * (float) entry.getValue() / maxBooks);
            row.addView(bar, new LinearLayout.LayoutParams(Math.max(dp(4), barW), dp(12)));

            TextView val = new TextView(this);
            val.setText(" " + entry.getValue());
            val.setTextColor(C.TEXT_D);
            val.setTextSize(12);
            row.addView(val);

            contentInner.addView(row);
        }
    }

    private View statCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        card.setPadding(dp(8), dp(12), dp(8), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(dp(3), 0, dp(3), 0);
        card.setLayoutParams(lp);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(C.ACCENT);
        v.setTextSize(18);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setGravity(Gravity.CENTER);
        card.addView(v);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(C.TEXT_D);
        l.setTextSize(10);
        l.setGravity(Gravity.CENTER);
        card.addView(l);

        return card;
    }

    private View buildCatBar(String cat, int count, int total) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        View dot = new View(this);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(C.catColor(cat));
        dot.setBackground(dotBg);
        row.addView(dot, new LinearLayout.LayoutParams(dp(8), dp(8)));

        TextView label = new TextView(this);
        label.setText("  " + cat);
        label.setTextColor(C.TEXT_M);
        label.setTextSize(12);
        label.setMinWidth(dp(120));
        row.addView(label);

        View bar = new View(this);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(C.catColor(cat));
        barBg.setCornerRadius(dp(3));
        bar.setBackground(barBg);
        int barW = (int)(dp(120) * (float) count / Math.max(1, total));
        row.addView(bar, new LinearLayout.LayoutParams(Math.max(dp(4), barW), dp(10)));

        TextView val = new TextView(this);
        val.setText(" " + count);
        val.setTextColor(C.TEXT_D);
        val.setTextSize(12);
        row.addView(val);

        return row;
    }

    private View buildRatingBar(int rating, int count, int total) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) stars.append("★");
        TextView label = new TextView(this);
        label.setText(stars.toString());
        label.setTextColor(C.GOLD);
        label.setTextSize(12);
        label.setMinWidth(dp(60));
        row.addView(label);

        View bar = new View(this);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(C.GOLD);
        barBg.setCornerRadius(dp(3));
        bar.setBackground(barBg);
        int barW = total > 0 ? (int)(dp(120) * (float) count / total) : 0;
        row.addView(bar, new LinearLayout.LayoutParams(Math.max(count > 0 ? dp(4) : 0, barW), dp(10)));

        TextView val = new TextView(this);
        val.setText(" " + count);
        val.setTextColor(C.TEXT_D);
        val.setTextSize(12);
        row.addView(val);

        return row;
    }

    // ═══════════════════════════════════════════
    //  REVIEW TAB
    // ═══════════════════════════════════════════
    private void renderReview() {
        contentInner.removeAllViews();
        contentInner.addView(sectionTitle("Year in Review"));

        List<Book> completed = ds.getCompleted();
        if (completed.size() < 3) {
            addSpacer(40);
            TextView msg = new TextView(this);
            msg.setText("Complete at least 3 books to unlock\nYear in Review.");
            msg.setTextColor(C.TEXT_M);
            msg.setTextSize(16);
            msg.setTypeface(Typeface.SERIF, Typeface.ITALIC);
            msg.setGravity(Gravity.CENTER);
            contentInner.addView(msg, new LinearLayout.LayoutParams(-1, -2));
            return;
        }

        // Top rated
        addSpacer(8);
        contentInner.addView(sectionTitle("Highest Rated"));
        List<Book> byRating = new ArrayList<>(completed);
        Collections.sort(byRating, (a, b) -> b.rating - a.rating);
        int top = Math.min(3, byRating.size());
        for (int i = 0; i < top; i++) {
            Book b = byRating.get(i);
            contentInner.addView(buildReviewBookItem(b, (i + 1) + "."));
        }

        // Milestones
        addSpacer(16);
        contentInner.addView(sectionTitle("Milestones"));

        // Simple milestones based on count
        List<String[]> milestones = new ArrayList<>();
        if (completed.size() >= 1)
            milestones.add(new String[]{"📖", "First Book", completed.get(0).title});
        if (completed.size() >= 5)
            milestones.add(new String[]{"📚", "5 Books", "Reached with " + completed.get(4).title});
        if (completed.size() >= 10)
            milestones.add(new String[]{"⭐", "10 Books", "Reached with " + completed.get(9).title});

        int totalPages = ds.totalPages();
        if (totalPages >= 2000)
            milestones.add(new String[]{"📏", String.format("%,d Pages", totalPages), "and counting"});

        int fiveStars = 0;
        for (Book b : completed) if (b.rating == 5) fiveStars++;
        if (fiveStars >= 1)
            milestones.add(new String[]{"⭐", "First 5-Star", "Discerning taste"});

        for (String[] m : milestones) {
            LinearLayout mi = new LinearLayout(this);
            mi.setOrientation(LinearLayout.HORIZONTAL);
            mi.setGravity(Gravity.CENTER_VERTICAL);
            GradientDrawable mBg = new GradientDrawable();
            mBg.setColor(C.SURFACE);
            mBg.setCornerRadius(dp(10));
            mi.setBackground(mBg);
            mi.setPadding(dp(12), dp(10), dp(12), dp(10));
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-1, -2);
            mlp.setMargins(0, dp(4), 0, dp(4));
            mi.setLayoutParams(mlp);

            TextView icon = new TextView(this);
            icon.setText(m[0]);
            icon.setTextSize(20);
            mi.addView(icon, new LinearLayout.LayoutParams(dp(36), -2));

            LinearLayout txt = new LinearLayout(this);
            txt.setOrientation(LinearLayout.VERTICAL);
            TextView lbl = new TextView(this);
            lbl.setText(m[1]);
            lbl.setTextColor(C.TEXT);
            lbl.setTextSize(14);
            lbl.setTypeface(Typeface.DEFAULT_BOLD);
            txt.addView(lbl);
            TextView det = new TextView(this);
            det.setText(m[2]);
            det.setTextColor(C.TEXT_D);
            det.setTextSize(12);
            txt.addView(det);
            mi.addView(txt, new LinearLayout.LayoutParams(0, -2, 1));
        }

        for (String[] m : milestones) {
            // Already built above but not added — rebuild properly
        }
        // Rebuild milestones (fixing the loop above)
        contentInner.removeViewAt(contentInner.getChildCount() - 1); // remove "Milestones" title, re-add with items
        contentInner.addView(sectionTitle("Milestones"));
        for (String[] m : milestones) {
            contentInner.addView(buildMilestoneItem(m[0], m[1], m[2]));
        }

        // Category diversity
        addSpacer(16);
        Set<String> cats = new HashSet<>();
        for (Book b : completed) cats.add(b.category);
        TextView diversity = new TextView(this);
        diversity.setText("Categories explored: " + cats.size() + " of " + C.CATEGORIES.length);
        diversity.setTextColor(C.TEXT_M);
        diversity.setTextSize(13);
        diversity.setGravity(Gravity.CENTER);
        contentInner.addView(diversity, new LinearLayout.LayoutParams(-1, -2));
    }

    private View buildReviewBookItem(Book b, String rank) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(0, dp(8), 0, dp(8));
        item.setOnClickListener(v -> openDetail(b.id));

        TextView r = new TextView(this);
        r.setText(rank);
        r.setTextColor(C.GOLD);
        r.setTextSize(16);
        r.setTypeface(Typeface.DEFAULT_BOLD);
        r.setMinWidth(dp(28));
        item.addView(r);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText(b.title);
        t.setTextColor(C.TEXT);
        t.setTextSize(14);
        t.setTypeface(Typeface.SERIF);
        info.addView(t);
        TextView a = new TextView(this);
        StringBuilder sb = new StringBuilder();
        if (b.author != null) sb.append(b.author);
        if (b.rating > 0) {
            sb.append(" · ");
            for (int i = 0; i < b.rating; i++) sb.append("★");
        }
        a.setText(sb.toString());
        a.setTextColor(C.TEXT_D);
        a.setTextSize(12);
        info.addView(a);
        item.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        return item;
    }

    private View buildMilestoneItem(String icon, String label, String detail) {
        LinearLayout mi = new LinearLayout(this);
        mi.setOrientation(LinearLayout.HORIZONTAL);
        mi.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(10));
        mi.setBackground(bg);
        mi.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(4), 0, dp(4));
        mi.setLayoutParams(lp);

        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(20);
        mi.addView(ic, new LinearLayout.LayoutParams(dp(36), -2));

        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(C.TEXT);
        l.setTextSize(14);
        l.setTypeface(Typeface.DEFAULT_BOLD);
        txt.addView(l);
        TextView d = new TextView(this);
        d.setText(detail);
        d.setTextColor(C.TEXT_D);
        d.setTextSize(12);
        txt.addView(d);
        mi.addView(txt, new LinearLayout.LayoutParams(0, -2, 1));

        return mi;
    }

    // ═══════════════════════════════════════════
    //  DETAIL (opens BookDetailActivity)
    // ═══════════════════════════════════════════
    private void openDetail(String bookId) {
        Intent i = new Intent(this, BookDetailActivity.class);
        i.putExtra("bookId", bookId);
        startActivity(i);
    }

    // ═══════════════════════════════════════════
    //  ADD FORM (opens BookFormActivity)
    // ═══════════════════════════════════════════
    private void openAddForm() {
        Intent i = new Intent(this, BookFormActivity.class);
        startActivity(i);
    }

    // ═══════════════════════════════════════════
    //  SETTINGS (opens SettingsActivity)
    // ═══════════════════════════════════════════
    private void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    // ═══════════════════════════════════════════
    //  ONBOARDING
    // ═══════════════════════════════════════════
    private void showOnboarding() {
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog);
        b.setTitle("Welcome to The Reading Ledger");
        b.setMessage("Track every book you read with beautiful shelves, detailed analytics, and a personal year in review.\n\nWould you like to start fresh or load a demo library?");
        b.setPositiveButton("Start Fresh", (d, w) -> renderAll());
        b.setNegativeButton("Load Demo (12 books)", (d, w) -> {
            ds.loadDemoData();
            renderAll();
        });
        b.setCancelable(false);
        b.show();
    }

    // ═══════════════════════════════════════════
    //  FAB
    // ═══════════════════════════════════════════
    private void addFab() {
        // FAB is overlaid on the root layout
        // Remove existing FAB if any
        View existing = rootLayout.findViewWithTag("fab");
        if (existing != null) ((ViewGroup) existing.getParent()).removeView(existing);

        // We need a FrameLayout wrapper — but since rootLayout is LinearLayout,
        // add FAB to contentScroll's parent or use a different approach
        // For simplicity, add a clickable button at the bottom of content
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════
    private int dp(int dp) { return C.dp(this, dp); }

    private void addSpacer(int dpHeight) {
        View s = new View(this);
        contentInner.addView(s, new LinearLayout.LayoutParams(-1, dp(dpHeight)));
    }

    private TextView sectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C.TEXT_M);
        tv.setTextSize(13);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setLetterSpacing(0.05f);
        tv.setPadding(0, dp(12), 0, dp(6));
        return tv;
    }

    private Button styledButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(C.BG);
        btn.setTextSize(14);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.ACCENT);
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);
        btn.setPadding(dp(28), dp(12), dp(28), dp(12));
        btn.setAllCaps(false);
        return btn;
    }

    @Override
    public void onBackPressed() {
        if (searchOpen) {
            toggleSearch();
        } else if (!"shelf".equals(currentTab)) {
            switchTab("shelf");
        } else {
            super.onBackPressed();
        }
    }
}
