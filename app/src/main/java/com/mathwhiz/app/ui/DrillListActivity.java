package com.mathwhiz.app.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.DrillRepository;
import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.Grade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The teacher's saved drills. Tap one to run it in class mode again. */
public class DrillListActivity extends AppCompatActivity {
    private LinearLayout list;
    private ProgressBar progress;
    private TextView tvEmpty, tvError;
    private List<DrillRepository.Entry> entries = new ArrayList<>();
    private final Set<String> expandedSections = new HashSet<>();
    private View openRow;
    private int touchSlop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_list);
        list = findViewById(R.id.list);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvError = findViewById(R.id.tvError);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        loadDrills();
    }

    private void loadDrills() {
        String uid = AuthRepository.get().currentUid();
        if (uid == null) return;
        UiUtils.show(progress, true);
        UiUtils.show(tvEmpty, false);
        UiUtils.showError(tvError, null);
        DrillRepository.get().loadDrills(uid, new DataCallback<List<DrillRepository.Entry>>() {
            @Override
            public void onSuccess(List<DrillRepository.Entry> entries) {
                UiUtils.show(progress, false);
                render(entries);
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                UiUtils.showError(tvError, message);
            }
        });
    }

    private void render(List<DrillRepository.Entry> entries) {
        this.entries = new ArrayList<>(entries);
        openRow = null;
        list.removeAllViews();
        UiUtils.show(tvEmpty, entries.isEmpty());

        Map<String, Map<String, List<DrillRepository.Entry>>> groups = new LinkedHashMap<>();
        for (final DrillRepository.Entry e : entries) {
            Drill drill = e.drill;
            String grade = drill.grade == null ? "" : drill.grade;
            String section = sectionName(drill);
            Map<String, List<DrillRepository.Entry>> sections = groups.get(grade);
            if (sections == null) {
                sections = new LinkedHashMap<>();
                groups.put(grade, sections);
            }
            List<DrillRepository.Entry> sectionDrills = sections.get(section);
            if (sectionDrills == null) {
                sectionDrills = new ArrayList<>();
                sections.put(section, sectionDrills);
            }
            sectionDrills.add(e);
        }

        for (Map.Entry<String, Map<String, List<DrillRepository.Entry>>> grade : groups.entrySet()) {
            addHeading(Grade.label(grade.getKey()), true);
            for (Map.Entry<String, List<DrillRepository.Entry>> section : grade.getValue().entrySet()) {
                addSection(section.getKey(), grade.getKey(), section.getValue());
            }
        }
    }

    private void addSection(String sectionName, String grade, List<DrillRepository.Entry> sectionDrills) {
        String sectionKey = grade + "\u0000" + sectionName;
        LinearLayout sectionBlock = new LinearLayout(this);
        sectionBlock.setOrientation(LinearLayout.VERTICAL);
        sectionBlock.setPadding(dp(12), 0, 0, dp(4));
        list.addView(sectionBlock, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addHeading(sectionName, false, sectionBlock);

        boolean expanded = expandedSections.contains(sectionKey);
        int count = expanded ? sectionDrills.size() : Math.min(2, sectionDrills.size());
        for (int i = 0; i < count; i++) addDrillRow(sectionDrills.get(i), sectionBlock);

        if (sectionDrills.size() > 2) {
            Button viewAll = new Button(this);
            viewAll.setText(expanded ? "Show less" : "View all (" + sectionDrills.size() + ")");
            viewAll.setAllCaps(false);
            viewAll.setTextSize(16);
            sectionBlock.addView(viewAll, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            viewAll.setOnClickListener(v -> {
                if (expandedSections.contains(sectionKey)) expandedSections.remove(sectionKey);
                else expandedSections.add(sectionKey);
                render(entries);
            });
        }
    }

    private String sectionName(Drill drill) {
        String section = drill.section == null ? "" : drill.section.trim();
        String prefix = Grade.label(drill.grade) + " - ";
        if (section.startsWith(prefix)) section = section.substring(prefix.length()).trim();
        return section.isEmpty() ? "Unspecified section" : section;
    }

    private void addHeading(String title, boolean gradeHeading) {
        addHeading(title, gradeHeading, list);
    }

    private void addHeading(String title, boolean gradeHeading, LinearLayout parent) {
        TextView heading = new TextView(this);
        heading.setText(title.isEmpty() ? "Other grade" : title);
        heading.setTextSize(gradeHeading ? 26 : 20);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(getColor(gradeHeading ? R.color.mw_green_dark : R.color.mw_ink));
        float density = getResources().getDisplayMetrics().density;
        int top = (int) ((gradeHeading ? 16 : 8) * density);
        heading.setPadding(0, top, 0, (int) (4 * density));
        parent.addView(heading);
    }

    private void addDrillRow(DrillRepository.Entry entry, LinearLayout parent) {
        FrameLayout container = new FrameLayout(this);
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        int actionWidth = dp(252);
        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
                actionWidth, dp(80), Gravity.END | Gravity.CENTER_VERTICAL);
        container.addView(actions, actionParams);
        addAction(actions, "Edit", R.color.mw_blue, () -> editDrill(entry));
        addAction(actions, "Delete", R.color.mw_red, () -> confirmDelete(entry));
        addAction(actions, "Duplicate", R.color.mw_green, () -> duplicateDrill(entry));

        View row = LayoutInflater.from(this).inflate(R.layout.item_row, container, false);
        Drill drill = entry.drill;
        ((TextView) row.findViewById(R.id.tvRowTitle)).setText(drill.title);
        String detail = drill.questionCount + " questions  \u2022  "
                + (drill.formatIsMultipleChoice() ? "Multiple choice" : "Flashcard")
                + (drill.modeIsTimed() ? "  \u2022  Timed " + drill.timeLimitSec + "s" : "  \u2022  Untimed");
        ((TextView) row.findViewById(R.id.tvRowSub)).setText(detail);
        row.setOnClickListener(v -> {
            if (row.getTranslationX() < 0) {
                closeRow(row);
            } else {
                Intent i = new Intent(this, ClassModeActivity.class);
                i.putExtra(IntentKeys.DRILL, entry.drill);
                i.putExtra(IntentKeys.DRILL_ID, entry.id);
                startActivity(i);
            }
        });
        row.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;
            private float startTranslation;
            private boolean dragging;
            private boolean scrolling;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startTranslation = row.getTranslationX();
                        dragging = false;
                        scrolling = false;
                        row.getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - downX;
                        float deltaY = event.getRawY() - downY;
                        if (!dragging && Math.abs(deltaY) > touchSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                            scrolling = true;
                            row.getParent().requestDisallowInterceptTouchEvent(false);
                            return false;
                        }
                        if (!dragging && Math.abs(deltaX) > touchSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
                            dragging = true;
                            if (openRow != null && openRow != row) closeRow(openRow);
                            openRow = row;
                        }
                        if (dragging) {
                            row.setTranslationX(Math.max(-actionWidth, Math.min(0, startTranslation + deltaX)));
                            return true;
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (dragging) {
                            if (row.getTranslationX() < -actionWidth / 2f) openRow(row, actionWidth);
                            else closeRow(row);
                        } else if (!scrolling) {
                            row.performClick();
                        }
                        row.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        if (dragging) closeRow(row);
                        row.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    default:
                        return true;
                }
            }
        });
        FrameLayout.LayoutParams rowParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(10);
        container.addView(row, rowParams);
        row.addOnLayoutChangeListener((view, left, top, right, bottom,
                                      oldLeft, oldTop, oldRight, oldBottom) -> {
            int rowHeight = bottom - top;
            if (rowHeight > 0 && actionParams.height != rowHeight) {
                actionParams.height = rowHeight;
                actions.setLayoutParams(actionParams);
            }
        });
        parent.addView(container, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addAction(LinearLayout actions, String title, int color, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(12);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(0);
        button.setBackgroundTintList(ColorStateList.valueOf(getColor(color)));
        actions.addView(button, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        button.setOnClickListener(v -> {
            if (openRow != null) closeRow(openRow);
            action.run();
        });
    }

    private void openRow(View row, int width) {
        row.animate().translationX(-width).setDuration(160).start();
        openRow = row;
    }

    private void closeRow(View row) {
        row.animate().translationX(0).setDuration(160).start();
        if (openRow == row) openRow = null;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void editDrill(DrillRepository.Entry entry) {
        Intent intent = new Intent(this, CreateDrillActivity.class);
        intent.putExtra(IntentKeys.DRILL, entry.drill);
        intent.putExtra(IntentKeys.DRILL_ID, entry.id);
        startActivity(intent);
    }

    private void confirmDelete(DrillRepository.Entry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete drill?")
                .setMessage("Delete \"" + entry.drill.title + "\"? This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteDrill(entry))
                .show();
    }

    private void deleteDrill(DrillRepository.Entry entry) {
        UiUtils.show(progress, true);
        DrillRepository.get().deleteDrill(entry.id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                UiUtils.show(progress, false);
                loadDrills();
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                UiUtils.showError(tvError, message);
            }
        });
    }

    private void duplicateDrill(DrillRepository.Entry entry) {
        Drill original = entry.drill;
        Drill copy = new Drill();
        copy.teacherUid = original.teacherUid;
        copy.title = duplicateTitle(original.title);
        copy.grade = original.grade;
        copy.section = original.section;
        copy.skillId = original.skillId;
        copy.topic = original.topic;
        copy.minNumber = original.minNumber;
        copy.maxNumber = original.maxNumber;
        copy.questionCount = original.questionCount;
        copy.format = original.format;
        copy.mode = original.mode;
        copy.timeLimitSec = original.timeLimitSec;
        copy.createdAt = System.currentTimeMillis();
        UiUtils.show(progress, true);
        DrillRepository.get().saveDrill(copy, new DataCallback<DrillRepository.Entry>() {
            @Override
            public void onSuccess(DrillRepository.Entry value) {
                UiUtils.show(progress, false);
                UiUtils.toast(DrillListActivity.this, "Drill duplicated.");
                loadDrills();
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                UiUtils.showError(tvError, message);
            }
        });
    }

    private String duplicateTitle(String title) {
        String prefix = "Copy of ";
        String original = title == null ? "" : title;
        int maxOriginalLength = 80 - prefix.length();
        if (original.length() > maxOriginalLength) original = original.substring(0, maxOriginalLength);
        return prefix + original;
    }

}
