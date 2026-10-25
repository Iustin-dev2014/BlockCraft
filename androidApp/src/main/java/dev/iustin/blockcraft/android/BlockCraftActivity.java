package dev.iustin.blockcraft.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BlockCraftActivity extends Activity {
    private static final String PREFS = "blockcraft_worlds";
    private static final String WORLD_NAMES = "names";
    private GLSurfaceView surface;
    private LinearLayout root;
    private ArrayList<String> worlds = new ArrayList<>();
    private ArrayAdapter<String> worldAdapter;
    private String selectedWorld;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadWorlds();
        showMainMenu();
    }

    private void loadWorlds() {
        Set<String> saved = getSharedPreferences(PREFS, MODE_PRIVATE).getStringSet(WORLD_NAMES, new HashSet<>());
        worlds = new ArrayList<>(saved);
        Collections.sort(worlds, String.CASE_INSENSITIVE_ORDER);
        if (!worlds.isEmpty()) selectedWorld = worlds.get(0);
    }

    private void saveWorlds() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putStringSet(WORLD_NAMES, new HashSet<>(worlds)).apply();
    }

    private void showMainMenu() {
        surface = null;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(18), dp(24), dp(18));
        root.setBackgroundColor(0xFF172B3A);

        TextView logo = new TextView(this);
        logo.setText("🟩 BLOCKCRAFT");
        logo.setTextColor(0xFFFFFFFF);
        logo.setTextSize(36);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(null, Typeface.BOLD);
        root.addView(logo, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView subtitle = new TextView(this);
        subtitle.setText("BUILD  •  EXPLORE  •  CREATE");
        subtitle.setTextColor(0xFF9ED9A5);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(34)));

        addMenuButton("JOACĂ", v -> {
            if (worlds.isEmpty()) {
                Toast.makeText(this, "Creează mai întâi o lume!", Toast.LENGTH_SHORT).show();
                showCreateWorldDialog();
            } else {
                showWorldPicker();
            }
        });
        addMenuButton("CREEAZĂ O LUME NOUĂ", v -> showCreateWorldDialog());
        addMenuButton("LUMILE MELE (" + worlds.size() + ")", v -> showWorldPicker());
        addMenuButton("SETĂRI", v -> showSettings());
        addMenuButton("IEȘIRE", v -> finish());

        TextView footer = new TextView(this);
        footer.setText("BlockCraft • versiune în dezvoltare");
        footer.setTextColor(0xFFB9C7D0);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(42));
        fp.topMargin = dp(12);
        root.addView(footer, fp);
        setContentView(root);
    }

    private void addMenuButton(String label, View.OnClickListener action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF287A52));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.topMargin = dp(7);
        root.addView(button, params);
        button.setOnClickListener(action);
    }

    private void showCreateWorldDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Numele lumii");
        input.setText("Lume nouă " + (worlds.size() + 1));
        new AlertDialog.Builder(this)
                .setTitle("Creează o lume")
                .setMessage("Lumea va fi salvată pe acest dispozitiv.")
                .setView(input)
                .setNegativeButton("ANULEAZĂ", (d, w) -> { })
                .setPositiveButton("CREEAZĂ", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Lume nouă";
                    String base = name;
                    int suffix = 2;
                    while (containsWorld(name)) name = base + " (" + suffix++ + ")";
                    worlds.add(name);
                    Collections.sort(worlds, String.CASE_INSENSITIVE_ORDER);
                    selectedWorld = name;
                    saveWorlds();
                    Toast.makeText(this, "Lume creată: " + name, Toast.LENGTH_SHORT).show();
                    showWorldPicker();
                }).show();
    }

    private boolean containsWorld(String name) {
        for (String world : worlds) if (world.equalsIgnoreCase(name)) return true;
        return false;
    }

    private void showWorldPicker() {
        if (worlds.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Lumile mele")
                    .setMessage("Nu ai încă lumi. Creează prima ta lume!")
                    .setPositiveButton("CREEAZĂ", (d, w) -> showCreateWorldDialog())
                    .setNegativeButton("ÎNAPOI", (d, w) -> showMainMenu()).show();
            return;
        }
        String[] items = worlds.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("Alege o lume")
                .setSingleChoiceItems(items, Math.max(0, worlds.indexOf(selectedWorld)),
                        (dialog, which) -> selectedWorld = worlds.get(which))
                .setPositiveButton("JOACĂ", (dialog, which) -> launchWorld())
                .setNeutralButton("ȘTERGE", (dialog, which) -> confirmDeleteWorld())
                .setNegativeButton("ÎNAPOI", (dialog, which) -> showMainMenu())
                .show();
    }

    private void confirmDeleteWorld() {
        if (selectedWorld == null) return;
        String deleting = selectedWorld;
        new AlertDialog.Builder(this).setTitle("Ștergi lumea?")
                .setMessage("„" + deleting + "” va fi eliminată din lista locală.")
                .setNegativeButton("ANULEAZĂ", (d, w) -> showWorldPicker())
                .setPositiveButton("ȘTERGE", (d, w) -> {
                    worlds.remove(deleting);
                    selectedWorld = worlds.isEmpty() ? null : worlds.get(0);
                    saveWorlds();
                    showWorldPicker();
                }).show();
    }

    private void launchWorld() {
        if (selectedWorld == null) return;
        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        surface.setRenderer(new BlockCraftRenderer());
        surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(surface);
        Toast.makeText(this, "Lume: " + selectedWorld + " • ESC / Înapoi pentru meniu", Toast.LENGTH_LONG).show();
    }

    private void showSettings() {
        new AlertDialog.Builder(this).setTitle("Setări")
                .setMessage("Setările grafice și controalele personalizabile vor fi adăugate în următoarele etape.\n\nPlatformă: Android 10+")
                .setPositiveButton("OK", (d, w) -> showMainMenu()).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onBackPressed() {
        if (surface != null) {
            if (surface != null) surface.onPause();
            showMainMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onPause() {
        if (surface != null) surface.onPause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (surface != null) surface.onResume();
    }
}
