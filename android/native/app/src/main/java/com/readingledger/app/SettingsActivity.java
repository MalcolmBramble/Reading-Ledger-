package com.readingledger.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.readingledger.app.db.DataStore;
import com.readingledger.app.util.C;
import java.io.*;

public class SettingsActivity extends Activity {
    private static final int FILE_PICK = 1;
    private DataStore ds;
    private LinearLayout content;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ds = DataStore.get(this);
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
        title.setText("Settings");
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
        addSpacer(20);

        // Annual Goal
        settingsItem("Annual Reading Goal", null);
        LinearLayout goalRow = new LinearLayout(this);
        goalRow.setOrientation(LinearLayout.HORIZONTAL);
        goalRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText goalInput = editField(String.valueOf(ds.getAnnualGoal()));
        goalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        goalRow.addView(goalInput, new LinearLayout.LayoutParams(dp(80), -2));
        TextView goalLabel = new TextView(this);
        goalLabel.setText("  books per year");
        goalLabel.setTextColor(C.TEXT_D);
        goalLabel.setTextSize(13);
        goalRow.addView(goalLabel);
        Button goalSave = secondaryButton("Set");
        goalSave.setOnClickListener(new android.view.View.OnClickListener() { @Override public void onClick(android.view.View v) {
            int g = parseInt(goalInput.getText().toString());
            if (g > 0) { ds.setAnnualGoal(g); Toast.makeText(SettingsActivity.this, "Goal set to " + g, Toast.LENGTH_SHORT).show(); }
        }});
        goalRow.addView(goalSave);
        content.addView(goalRow);

        addSpacer(20);

        // Export
        settingsItem("Export Library", null);
        LinearLayout expRow = new LinearLayout(this);
        expRow.setOrientation(LinearLayout.HORIZONTAL);
        Button expJson = secondaryButton("Export JSON");
        expJson.setOnClickListener(v -> exportJson());
        expRow.addView(expJson, new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(expRow);

        String lastExp = ds.getLastExport();
        if (lastExp != null) {
            TextView lastExpText = new TextView(this);
            lastExpText.setText("Last export: " + C.fmtDate(lastExp));
            lastExpText.setTextColor(C.TEXT_D);
            lastExpText.setTextSize(11);
            lastExpText.setPadding(0, dp(4), 0, 0);
            content.addView(lastExpText);
        }

        addSpacer(20);

        // Import
        settingsItem("Import Library", null);
        Button impBtn = secondaryButton("Import JSON");
        impBtn.setOnClickListener(new android.view.View.OnClickListener() { @Override public void onClick(android.view.View v) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("application/json");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(i, "Select backup file"), FILE_PICK);
        }});
        content.addView(impBtn);

        addSpacer(20);

        // Reset demo
        settingsItem("Reset Demo", null);
        Button resetBtn = secondaryButton("Reload Sample Data");
        resetBtn.setOnClickListener(new android.view.View.OnClickListener() { @Override public void onClick(android.view.View v) {
            new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Reset")
                .setMessage("Replace your library with demo data?")
                .setPositiveButton("Reset", (d, w) -> { ds.loadDemoData(); finish(); })
                .setNegativeButton("Cancel", null)
                .show();
        }});
        content.addView(resetBtn);

        addSpacer(20);

        // Clear all
        settingsItem("Clear All", null);
        Button clearBtn = new Button(this);
        clearBtn.setText("Clear Everything");
        clearBtn.setTextColor(C.RED);
        clearBtn.setTextSize(13);
        GradientDrawable clearBg = new GradientDrawable();
        clearBg.setColor(0x00000000);
        clearBg.setCornerRadius(dp(8));
        clearBg.setStroke(dp(1), C.RED);
        clearBtn.setBackground(clearBg);
        clearBtn.setPadding(dp(16), dp(8), dp(16), dp(8));
        clearBtn.setAllCaps(false);
        clearBtn.setOnClickListener(new android.view.View.OnClickListener() { @Override public void onClick(android.view.View v) {
            new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Clear All")
                .setMessage("Delete everything? This cannot be undone.")
                .setPositiveButton("Clear", (d, w) -> { ds.clearAll(); finish(); })
                .setNegativeButton("Cancel", null)
                .show();
        }});
        content.addView(clearBtn);

        addSpacer(20);

        // App info
        TextView info = new TextView(this);
        info.setText("The Reading Ledger · Native v1.0\ncom.readingledger.app");
        info.setTextColor(C.TEXT_D);
        info.setTextSize(11);
        info.setGravity(Gravity.CENTER);
        content.addView(info, new LinearLayout.LayoutParams(-1, -2));
    }

    private void exportJson() {
        String json = ds.exportJson();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_TEXT, json);
        share.putExtra(Intent.EXTRA_SUBJECT, "reading-ledger-backup-" + C.today() + ".json");
        startActivity(Intent.createChooser(share, "Export Library"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_PICK && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String json = sb.toString();
                new AlertDialog.Builder(this)
                    .setTitle("Import")
                    .setMessage("How would you like to import?")
                    .setPositiveButton("Replace", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) {
                        if (ds.importJson(json, false)) {
                            Toast.makeText(SettingsActivity.this, "Library replaced", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Invalid file", Toast.LENGTH_SHORT).show();
                        }
                    } })
                    .setNegativeButton("Merge", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) {
                        if (ds.importJson(json, true)) {
                            Toast.makeText(SettingsActivity.this, "Library merged", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Invalid file", Toast.LENGTH_SHORT).show();
                        }
                    } })
                    .setNeutralButton("Cancel", null)
                    .show();
            } catch (Exception e) {
                Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void settingsItem(String label, String desc) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(C.TEXT);
        l.setTextSize(15);
        l.setTypeface(Typeface.DEFAULT_BOLD);
        l.setPadding(0, 0, 0, dp(4));
        content.addView(l);
        if (desc != null) {
            TextView d = new TextView(this);
            d.setText(desc);
            d.setTextColor(C.TEXT_D);
            d.setTextSize(12);
            d.setPadding(0, 0, 0, dp(6));
            content.addView(d);
        }
    }

    private Button secondaryButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(C.ACCENT);
        btn.setTextSize(13);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), C.BORDER);
        btn.setBackground(bg);
        btn.setPadding(dp(16), dp(8), dp(16), dp(8));
        btn.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(4), 0, dp(4), 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    private EditText editField(String value) {
        EditText et = new EditText(this);
        et.setText(value);
        et.setTextColor(C.TEXT);
        et.setTextSize(14);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C.SURFACE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), C.BORDER);
        et.setBackground(bg);
        et.setPadding(dp(12), dp(8), dp(12), dp(8));
        return et;
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private int dp(int dp) { return C.dp(this, dp); }

    private void addSpacer(int h) {
        View s = new View(this);
        content.addView(s, new LinearLayout.LayoutParams(-1, dp(h)));
    }
}
