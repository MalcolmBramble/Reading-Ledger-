package com.readingledger.app.util;

import android.graphics.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class C {
    // ═══ CATEGORIES ═══
    public static final String[] CATEGORIES = {
        "Self-Awareness", "Current America", "Economics & Money", "Technology",
        "American History", "Science", "World History", "Philosophy & Ethics",
        "Religion", "Fiction", "Other"
    };

    // Category colors (brighter, for labels/charts)
    public static final Map<String, Integer> CAT_COLORS = new LinkedHashMap<>();
    // Spine colors (darker, for book spines)
    public static final Map<String, Integer> CAT_SPINE = new LinkedHashMap<>();
    static {
        CAT_COLORS.put("Self-Awareness",      0xFFC4956A);
        CAT_COLORS.put("Current America",      0xFF7D8B6E);
        CAT_COLORS.put("Economics & Money",    0xFF3D5A80);
        CAT_COLORS.put("Technology",           0xFFC46B5B);
        CAT_COLORS.put("American History",     0xFFC45B72);
        CAT_COLORS.put("Science",              0xFF8B6AAC);
        CAT_COLORS.put("World History",        0xFF5B9EAD);
        CAT_COLORS.put("Philosophy & Ethics",  0xFF6EC4A7);
        CAT_COLORS.put("Religion",             0xFF9B7EC4);
        CAT_COLORS.put("Fiction",              0xFFBBA14F);
        CAT_COLORS.put("Other",                0xFF7A7670);

        CAT_SPINE.put("Self-Awareness",        0xFF8B6540);
        CAT_SPINE.put("Current America",       0xFF556B4A);
        CAT_SPINE.put("Economics & Money",     0xFF2E4460);
        CAT_SPINE.put("Technology",            0xFF8B4B3B);
        CAT_SPINE.put("American History",      0xFF8B3B4E);
        CAT_SPINE.put("Science",               0xFF5E4570);
        CAT_SPINE.put("World History",         0xFF3B7080);
        CAT_SPINE.put("Philosophy & Ethics",   0xFF408060);
        CAT_SPINE.put("Religion",              0xFF6B5090);
        CAT_SPINE.put("Fiction",               0xFF8B7530);
        CAT_SPINE.put("Other",                 0xFF5A5650);
    }

    public static int catColor(String cat) {
        Integer c = CAT_COLORS.get(cat);
        return c != null ? c : 0xFF7A7670;
    }
    public static int spineColor(String cat) {
        Integer c = CAT_SPINE.get(cat);
        return c != null ? c : 0xFF5A5650;
    }

    // ═══ THEME COLORS ═══
    public static final int BG         = 0xFF0E0C0A;
    public static final int SURFACE    = 0xFF1A1714;
    public static final int SURFACE2   = 0xFF23201B;
    public static final int BORDER     = 0xFF2E2A24;
    public static final int TEXT       = 0xFFE8E0D4;
    public static final int TEXT_M     = 0xFFA89E8C;
    public static final int TEXT_D     = 0xFF6B6459;
    public static final int ACCENT     = 0xFFA8B88E;
    public static final int ACCENT_DIM = 0xFF7D8B6E;
    public static final int GOLD       = 0xFFD4A847;
    public static final int GREEN      = 0xFF6EC4A7;
    public static final int BLUE       = 0xFF5B9EAD;
    public static final int RED        = 0xFFC46B5B;
    public static final int NAV_BG     = 0xF017140F; // 94% alpha

    // ═══ STATUS ═══
    public static final String S_WANT    = "want-to-read";
    public static final String S_READING = "reading";
    public static final String S_DONE    = "completed";
    public static final String S_DROPPED = "abandoned";

    public static String statusLabel(String s) {
        if (s == null) return "";
        switch (s) {
            case S_WANT: return "To Read";
            case S_READING: return "Reading";
            case S_DONE: return "Completed";
            case S_DROPPED: return "Dropped";
            default: return s;
        }
    }

    // ═══ DIMENSIONS (dp) ═══
    public static final int SPINE_W = 52;
    public static final int SPINE_H = 130;
    public static final int SPINES_PER_ROW = 6;
    public static final int NAV_H = 64;

    // ═══ UID GENERATOR ═══
    public static String uid() {
        return Long.toString(System.currentTimeMillis(), 36)
             + Long.toString((long)(Math.random() * 1e12), 36);
    }

    // ═══ DATE HELPERS ═══
    public static String today() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(new java.util.Date());
    }

    public static String isoNow() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            .format(new java.util.Date());
    }

    public static String fmtDate(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US);
            return out.format(in.parse(iso));
        } catch (Exception e) { return iso; }
    }

    public static String fmtShort(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("MMM d", java.util.Locale.US);
            return out.format(in.parse(iso));
        } catch (Exception e) { return iso; }
    }

    public static int dp(android.content.Context ctx, int dp) {
        return (int)(dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
