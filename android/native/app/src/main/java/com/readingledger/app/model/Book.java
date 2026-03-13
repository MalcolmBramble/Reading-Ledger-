package com.readingledger.app.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Book {
    public String id;
    public String title;
    public String author;
    public String category;
    public String status; // want-to-read, reading, completed, abandoned
    public int pages;
    public int currentPage;
    public int rating; // 0-5
    public String startDate;
    public String endDate;
    public String addedAt;
    public String updatedAt;
    public String notes;
    public String coreArgument;
    public String impact;
    public String recommendedBy;
    public String recommendationNote;
    public String recommendationSource;
    public int priority;
    public List<String> themes;
    public List<Quote> quotes;
    public List<String> connections;
    public List<Session> sessions;

    public Book() {
        themes = new ArrayList<>();
        quotes = new ArrayList<>();
        connections = new ArrayList<>();
        sessions = new ArrayList<>();
    }

    public int getProgressPercent() {
        if (pages <= 0) return 0;
        return Math.min(100, Math.round((float) currentPage / pages * 100));
    }

    public int getDaysToRead() {
        if (startDate == null || endDate == null) return 0;
        try {
            long start = java.text.SimpleDateFormat.getDateInstance().parse(startDate).getTime();
            long end = java.text.SimpleDateFormat.getDateInstance().parse(endDate).getTime();
            return Math.max(1, (int)((end - start) / 86400000L));
        } catch (Exception e) { return 0; }
    }

    public JSONObject toJson() {
        try {
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("title", title);
            j.put("author", author);
            j.put("category", category);
            j.put("status", status);
            j.put("pages", pages);
            j.put("currentPage", currentPage);
            j.put("rating", rating);
            j.put("startDate", startDate != null ? startDate : JSONObject.NULL);
            j.put("endDate", endDate != null ? endDate : JSONObject.NULL);
            j.put("addedAt", addedAt);
            j.put("updatedAt", updatedAt);
            j.put("notes", notes != null ? notes : "");
            j.put("coreArgument", coreArgument != null ? coreArgument : "");
            j.put("impact", impact != null ? impact : "");
            j.put("recommendedBy", recommendedBy != null ? recommendedBy : "");
            j.put("recommendationNote", recommendationNote != null ? recommendationNote : "");
            j.put("recommendationSource", recommendationSource != null ? recommendationSource : "");
            j.put("priority", priority);
            JSONArray ta = new JSONArray();
            for (String t : themes) ta.put(t);
            j.put("themes", ta);
            JSONArray qa = new JSONArray();
            for (Quote q : quotes) qa.put(q.toJson());
            j.put("quotes", qa);
            JSONArray ca = new JSONArray();
            for (String c : connections) ca.put(c);
            j.put("connections", ca);
            JSONArray sa = new JSONArray();
            for (Session s : sessions) sa.put(s.toJson());
            j.put("sessions", sa);
            return j;
        } catch (Exception e) { return new JSONObject(); }
    }

    public static Book fromJson(JSONObject j) {
        Book b = new Book();
        b.id = j.optString("id", "");
        b.title = j.optString("title", "");
        b.author = j.optString("author", "");
        b.category = j.optString("category", "Other");
        b.status = j.optString("status", "want-to-read");
        b.pages = j.optInt("pages", 0);
        b.currentPage = j.optInt("currentPage", 0);
        b.rating = j.optInt("rating", 0);
        b.startDate = j.isNull("startDate") ? null : j.optString("startDate", null);
        b.endDate = j.isNull("endDate") ? null : j.optString("endDate", null);
        b.addedAt = j.optString("addedAt", "");
        b.updatedAt = j.optString("updatedAt", "");
        b.notes = j.optString("notes", "");
        b.coreArgument = j.optString("coreArgument", "");
        b.impact = j.optString("impact", "");
        b.recommendedBy = j.optString("recommendedBy", "");
        b.recommendationNote = j.optString("recommendationNote", "");
        b.recommendationSource = j.optString("recommendationSource", "");
        b.priority = j.optInt("priority", 0);
        JSONArray ta = j.optJSONArray("themes");
        if (ta != null) for (int i = 0; i < ta.length(); i++) b.themes.add(ta.optString(i));
        JSONArray qa = j.optJSONArray("quotes");
        if (qa != null) for (int i = 0; i < qa.length(); i++) b.quotes.add(Quote.fromJson(qa.optJSONObject(i)));
        JSONArray ca = j.optJSONArray("connections");
        if (ca != null) for (int i = 0; i < ca.length(); i++) b.connections.add(ca.optString(i));
        JSONArray sa = j.optJSONArray("sessions");
        if (sa != null) for (int i = 0; i < sa.length(); i++) b.sessions.add(Session.fromJson(sa.optJSONObject(i)));
        return b;
    }

    public static class Quote {
        public String text;
        public String page;
        public String note;

        public JSONObject toJson() {
            try {
                JSONObject j = new JSONObject();
                j.put("text", text != null ? text : "");
                j.put("page", page != null ? page : "");
                j.put("note", note != null ? note : "");
                return j;
            } catch (Exception e) { return new JSONObject(); }
        }

        public static Quote fromJson(JSONObject j) {
            Quote q = new Quote();
            if (j == null) return q;
            q.text = j.optString("text", "");
            q.page = j.optString("page", "");
            q.note = j.optString("note", "");
            return q;
        }
    }

    public static class Session {
        public String date;
        public int duration; // minutes
        public int pagesRead;
        public String note;

        public JSONObject toJson() {
            try {
                JSONObject j = new JSONObject();
                j.put("date", date != null ? date : "");
                j.put("duration", duration);
                j.put("pagesRead", pagesRead);
                j.put("note", note != null ? note : "");
                return j;
            } catch (Exception e) { return new JSONObject(); }
        }

        public static Session fromJson(JSONObject j) {
            Session s = new Session();
            if (j == null) return s;
            s.date = j.optString("date", "");
            s.duration = j.optInt("duration", 0);
            s.pagesRead = j.optInt("pagesRead", 0);
            s.note = j.optString("note", "");
            return s;
        }
    }
}
