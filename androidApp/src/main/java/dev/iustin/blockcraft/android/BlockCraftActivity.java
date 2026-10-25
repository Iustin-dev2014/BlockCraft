package dev.iustin.blockcraft.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
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
    private BlockCraftRenderer gameRenderer;
    private TextView inventoryLabel;
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
        FrameLayout game = new FrameLayout(this);
        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        gameRenderer = new BlockCraftRenderer(this, selectedWorld);
        surface.setRenderer(gameRenderer);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        game.addView(surface, new FrameLayout.LayoutParams(-1, -1));

        inventoryLabel = new TextView(this);
        inventoryLabel.setTextColor(0xFFFFFFFF);
        inventoryLabel.setTextSize(12);
        inventoryLabel.setPadding(dp(10), dp(6), dp(10), dp(6));
        inventoryLabel.setBackgroundColor(0x990D1720);
        inventoryLabel.setText("BLOCKCRAFT  •  LUME 256 × 256");
        FrameLayout.LayoutParams status = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        status.topMargin = dp(8);
        game.addView(inventoryLabel, status);

        LinearLayout dpad = new LinearLayout(this);
        dpad.setOrientation(LinearLayout.VERTICAL);
        dpad.setGravity(Gravity.CENTER);
        addControl(dpad, "▲", () -> gameRenderer.setMovement(1, 0), () -> gameRenderer.setMovement(0, 0), true);
        LinearLayout middle = new LinearLayout(this);
        middle.setGravity(Gravity.CENTER);
        addControl(middle, "◀", () -> gameRenderer.setMovement(0, -1), () -> gameRenderer.setMovement(0, 0), true);
        addControl(middle, "▼", () -> gameRenderer.setMovement(-1, 0), () -> gameRenderer.setMovement(0, 0), true);
        addControl(middle, "▶", () -> gameRenderer.setMovement(0, 1), () -> gameRenderer.setMovement(0, 0), true);
        dpad.addView(middle);
        FrameLayout.LayoutParams dp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.LEFT);
        dp.leftMargin = dp(10); dp.bottomMargin = dp(12);
        game.addView(dpad, dp);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setGravity(Gravity.CENTER);
        addControl(actions, "⤴ SARI", () -> gameRenderer.jump(), null, false);
        addControl(actions, "SPARGE", () -> { gameRenderer.breakBlock(); updateInventoryLabel(); }, null, false);
        addControl(actions, "PUNE", () -> { gameRenderer.placeBlock(); updateInventoryLabel(); }, null, false);
        addControl(actions, "BLOC ▣", () -> { gameRenderer.nextBlock(); updateInventoryLabel(); }, null, false);
        LinearLayout look = new LinearLayout(this);
        look.setGravity(Gravity.CENTER);
        addControl(look, "⟲", () -> gameRenderer.turn(-12, 0), null, false);
        addControl(look, "⟳", () -> gameRenderer.turn(12, 0), null, false);
        addControl(look, "↑", () -> gameRenderer.turn(0, -8), null, false);
        addControl(look, "↓", () -> gameRenderer.turn(0, 8), null, false);
        actions.addView(look);
        FrameLayout.LayoutParams ap = new FrameLayout.LayoutParams(-2, -2, Gravity.RIGHT | Gravity.BOTTOM);
        ap.rightMargin = dp(10); ap.bottomMargin = dp(12);
        game.addView(actions, ap);

        Button back = new Button(this);
        back.setText("MENIU");
        back.setTextSize(10);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(78), dp(42), Gravity.TOP | Gravity.LEFT);
        bp.leftMargin = dp(8); bp.topMargin = dp(8);
        game.addView(back, bp);
        back.setOnClickListener(v -> { if (gameRenderer != null) gameRenderer.saveState(); surface.onPause(); gameRenderer = null; showMainMenu(); });
        setContentView(game);
        Toast.makeText(this, "Lume generată • folosește comenzile de pe ecran", Toast.LENGTH_LONG).show();
    }

    private void addControl(LinearLayout parent, String label, Runnable down, Runnable up, boolean hold) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(hold ? 17 : 11);
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(hold ? 0xAA245C42 : 0xDD243747));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(hold ? dp(54) : dp(68), dp(hold ? 52 : 42));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        parent.addView(b, lp);
        if (hold) {
            b.setOnTouchListener((v, e) -> {
                if (e.getAction() == MotionEvent.ACTION_DOWN) { down.run(); return true; }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) { if (up != null) up.run(); return true; }
                return true;
            });
        } else {
            b.setOnClickListener(v -> { down.run(); if (inventoryLabel != null && gameRenderer != null) updateInventoryLabel(); });
        }
    }

    private void updateInventoryLabel() {
        if (inventoryLabel != null && gameRenderer != null) inventoryLabel.setText(gameRenderer.inventoryText());
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
            if (gameRenderer != null) gameRenderer.saveState();
            if (surface != null) surface.onPause();
            gameRenderer = null;
            showMainMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onPause() {
        if (gameRenderer != null) gameRenderer.saveState();
        if (surface != null) surface.onPause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (surface != null) surface.onResume();
    }
}
