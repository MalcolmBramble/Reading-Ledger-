package com.readingledger.app.db;

import android.content.Context;
import android.content.SharedPreferences;
import com.readingledger.app.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Persists library data as JSON in SharedPreferences.
 * Uses the same JSON schema as the web app so import/export is seamless.
 */
public class DataStore {
    private static final String PREFS = "reading_ledger";
    private static final String KEY_DATA = "library_json";
    private static DataStore instance;

    private final SharedPreferences prefs;
    private List<Book> books;
    private int annualGoal;
    private String lastExport;
    private JSONObject categoryGoals;
    private JSONArray challenges;

    public static DataStore get(Context ctx) {
        if (instance == null) instance = new DataStore(ctx.getApplicationContext());
        return instance;
    }

    private DataStore(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    // ═══ LOAD / SAVE ═══
    private void load() {
        books = new ArrayList<>();
        annualGoal = 50;
        lastExport = null;
        categoryGoals = new JSONObject();
        challenges = new JSONArray();

        String json = prefs.getString(KEY_DATA, null);
        if (json == null) return;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray ba = root.optJSONArray("books");
            if (ba != null) {
                for (int i = 0; i < ba.length(); i++) {
                    books.add(Book.fromJson(ba.getJSONObject(i)));
                }
            }
            JSONObject settings = root.optJSONObject("settings");
            if (settings != null) {
                annualGoal = settings.optInt("goal", 50);
                lastExport = settings.isNull("lastExport") ? null : settings.optString("lastExport");
                JSONObject goals = settings.optJSONObject("goals");
                if (goals != null) {
                    annualGoal = goals.optInt("annual", annualGoal);
                    categoryGoals = goals.optJSONObject("categories");
                    if (categoryGoals == null) categoryGoals = new JSONObject();
                    challenges = goals.optJSONArray("challenges");
                    if (challenges == null) challenges = new JSONArray();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JSONObject root = new JSONObject();
            JSONArray ba = new JSONArray();
            for (Book b : books) ba.put(b.toJson());
            root.put("books", ba);

            JSONObject settings = new JSONObject();
            settings.put("goal", annualGoal);
            settings.put("lastExport", lastExport != null ? lastExport : JSONObject.NULL);
            JSONObject goals = new JSONObject();
            goals.put("annual", annualGoal);
            goals.put("categories", categoryGoals);
            goals.put("challenges", challenges);
            settings.put("goals", goals);
            root.put("settings", settings);

            prefs.edit().putString(KEY_DATA, root.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══ BOOKS ═══
    public List<Book> getBooks() { return books; }

    public Book getBook(String id) {
        for (Book b : books) if (b.id.equals(id)) return b;
        return null;
    }

    public void addBook(Book b) {
        books.add(b);
        save();
    }

    public void updateBook(Book b) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).id.equals(b.id)) {
                books.set(i, b);
                save();
                return;
            }
        }
    }

    public void deleteBook(String id) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).id.equals(id)) {
                books.remove(i);
                save();
                return;
            }
        }
    }

    // ═══ QUERIES ═══
    public List<Book> getCompleted() {
        List<Book> r = new ArrayList<>();
        for (Book b : books) if ("completed".equals(b.status)) r.add(b);
        return r;
    }

    public List<Book> getReading() {
        List<Book> r = new ArrayList<>();
        for (Book b : books) if ("reading".equals(b.status)) r.add(b);
        return r;
    }

    public List<Book> getByStatus(String status) {
        List<Book> r = new ArrayList<>();
        for (Book b : books) if (status.equals(b.status)) r.add(b);
        return r;
    }

    public List<Book> search(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;
        List<Book> r = new ArrayList<>();
        for (Book b : books) {
            if (b.title.toLowerCase().contains(q)
             || (b.author != null && b.author.toLowerCase().contains(q))
             || (b.category != null && b.category.toLowerCase().contains(q))
             || (b.notes != null && b.notes.toLowerCase().contains(q))) {
                r.add(b);
                continue;
            }
            for (String t : b.themes) if (t.toLowerCase().contains(q)) { r.add(b); break; }
        }
        return r;
    }

    public List<Book> sorted(List<Book> list, String sortBy) {
        List<Book> r = new ArrayList<>(list);
        switch (sortBy) {
            case "date":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return cmpStr(b.addedAt, a.addedAt); } });
                break;
            case "category":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return cmpStr(a.category, b.category); } });
                break;
            case "rating":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return b.rating - a.rating; } });
                break;
            case "length":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return b.pages - a.pages; } });
                break;
            case "status":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return statusOrd(a.status) - statusOrd(b.status); } });
                break;
            case "title":
                Collections.sort(r, new Comparator<Book>() { public int compare(Book a, Book b) { return cmpStr(a.title, b.title); } });
                break;
        }
        return r;
    }

    private int statusOrd(String s) {
        if ("reading".equals(s)) return 0;
        if ("completed".equals(s)) return 1;
        if ("want-to-read".equals(s)) return 2;
        if ("abandoned".equals(s)) return 3;
        return 4;
    }

    private int cmpStr(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareToIgnoreCase(b);
    }

    // ═══ SETTINGS ═══
    public int getAnnualGoal() { return annualGoal; }
    public void setAnnualGoal(int g) { annualGoal = g; save(); }
    public String getLastExport() { return lastExport; }
    public void setLastExport(String d) { lastExport = d; save(); }
    public JSONObject getCategoryGoals() { return categoryGoals; }
    public JSONArray getChallenges() { return challenges; }
    public void setChallenges(JSONArray c) { challenges = c; save(); }

    // ═══ IMPORT / EXPORT ═══
    public String exportJson() {
        try {
            JSONObject root = new JSONObject();
            JSONArray ba = new JSONArray();
            for (Book b : books) ba.put(b.toJson());
            root.put("books", ba);
            JSONObject settings = new JSONObject();
            settings.put("goal", annualGoal);
            settings.put("lastExport", com.readingledger.app.util.C.isoNow());
            JSONObject goals = new JSONObject();
            goals.put("annual", annualGoal);
            goals.put("categories", categoryGoals);
            goals.put("challenges", challenges);
            settings.put("goals", goals);
            root.put("settings", settings);
            lastExport = com.readingledger.app.util.C.isoNow();
            save();
            return root.toString(2);
        } catch (Exception e) { return "{}"; }
    }

    public boolean importJson(String json, boolean merge) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray ba = root.optJSONArray("books");
            if (ba == null) return false;

            if (merge) {
                java.util.Set<String> existing = new java.util.HashSet<>();
                for (Book b : books)
                    existing.add((b.title + "|" + b.author).toLowerCase());
                for (int i = 0; i < ba.length(); i++) {
                    Book b = Book.fromJson(ba.getJSONObject(i));
                    String key = (b.title + "|" + b.author).toLowerCase();
                    if (!existing.contains(key)) {
                        books.add(b);
                        existing.add(key);
                    }
                }
            } else {
                books.clear();
                for (int i = 0; i < ba.length(); i++) {
                    books.add(Book.fromJson(ba.getJSONObject(i)));
                }
                JSONObject settings = root.optJSONObject("settings");
                if (settings != null) {
                    annualGoal = settings.optInt("goal", 50);
                    JSONObject goals = settings.optJSONObject("goals");
                    if (goals != null) {
                        annualGoal = goals.optInt("annual", annualGoal);
                        categoryGoals = goals.optJSONObject("categories");
                        if (categoryGoals == null) categoryGoals = new JSONObject();
                        challenges = goals.optJSONArray("challenges");
                        if (challenges == null) challenges = new JSONArray();
                    }
                }
            }
            save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadDemoData() {
        String demoJson = DemoData.getJson();
        importJson(demoJson, false);
    }

    public void clearAll() {
        books.clear();
        annualGoal = 50;
        lastExport = null;
        categoryGoals = new JSONObject();
        challenges = new JSONArray();
        save();
    }

    // ═══ STATS ═══
    public int totalPages() {
        int t = 0;
        for (Book b : getCompleted()) t += b.pages;
        return t;
    }
}
