package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.DrillRepository;
import com.mathwhiz.app.logic.CurriculumGuide;
import com.mathwhiz.app.logic.DrillGenerator;
import com.mathwhiz.app.logic.Validators;
import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.Grade;
import com.mathwhiz.app.model.Skill;
import com.mathwhiz.app.model.Topic;

import java.util.ArrayList;
import java.util.List;

/** Drill setup: grade + section, skill from that grade's list, count, format, timed/untimed. */
public class CreateDrillActivity extends AppCompatActivity {
    private static final String[] GRADE_LABELS = {"Kindergarten", "Grade 1"};
    private static final String[] GRADE_VALUES = {Grade.KINDER, Grade.GRADE_1};
    private static final String[] FORMAT_LABELS = {"Flashcard", "Multiple choice"};
    private static final String[] MODE_LABELS = {"Untimed", "Timed"};

    private Spinner spGrade, spSkill, spFormat, spMode;
    private EditText etSection, etMax, etCount, etTime, etTitle;
    private TextView tvError;
    private View timeRow;
    private Button btnSave;
    private ProgressBar progress;
    private final List<Skill> skills = new ArrayList<>();
    private Drill editingDrill;
    private String editingDrillId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);
        spGrade = findViewById(R.id.spGrade);
        spSkill = findViewById(R.id.spSkill);
        spFormat = findViewById(R.id.spFormat);
        spMode = findViewById(R.id.spMode);
        etSection = findViewById(R.id.etSection);
        etMax = findViewById(R.id.etMax);
        etCount = findViewById(R.id.etCount);
        etTime = findViewById(R.id.etTime);
        etTitle = findViewById(R.id.etTitle);
        tvError = findViewById(R.id.tvError);
        timeRow = findViewById(R.id.timeRow);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        spGrade.setAdapter(adapter(GRADE_LABELS));
        spFormat.setAdapter(adapter(FORMAT_LABELS));
        spMode.setAdapter(adapter(MODE_LABELS));

        spGrade.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            void onChosen(int position) { loadSkillsFor(GRADE_VALUES[position]); }
        });
        spMode.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            void onChosen(int position) { UiUtils.show(timeRow, position == 1); }
        });
        if (getIntent().hasExtra(IntentKeys.DRILL)) {
            initializeFromDrill();
        } else {
            etMax.setText("10");
            etCount.setText("10");
            etTime.setText("20");
        }
        btnSave.setOnClickListener(v -> save());
    }

    private ArrayAdapter<String> adapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.item_spinner, items);
        a.setDropDownViewResource(R.layout.item_spinner);
        return a;
    }

    /** Only the chosen grade's skills are listed, so Kinder never shows Grade 1 content. */
    private void loadSkillsFor(String grade) {
        skills.clear();
        skills.addAll(CurriculumGuide.forGrade(grade));
        List<String> titles = new ArrayList<>();
        for (Skill s : skills) titles.add(Topic.label(s.topic));
        spSkill.setAdapter(adapter(titles.toArray(new String[0])));
    }

    @SuppressWarnings("deprecation")
    private void initializeFromDrill() {
        editingDrill = (Drill) getIntent().getSerializableExtra(IntentKeys.DRILL);
        editingDrillId = getIntent().getStringExtra(IntentKeys.DRILL_ID);
        if (editingDrill == null || editingDrillId == null) {
            UiUtils.showError(tvError, "This saved drill could not be edited.");
            finish();
            return;
        }
        int gradePosition = Grade.GRADE_1.equals(editingDrill.grade) ? 1 : 0;
        spGrade.setSelection(gradePosition);
        loadSkillsFor(editingDrill.grade);
        for (int i = 0; i < skills.size(); i++) {
            if (skills.get(i).topic.equals(editingDrill.topic)) {
                spSkill.setSelection(i);
                break;
            }
        }
        spFormat.setSelection(Drill.FORMAT_MULTIPLE_CHOICE.equals(editingDrill.format) ? 1 : 0);
        spMode.setSelection(Drill.MODE_TIMED.equals(editingDrill.mode) ? 1 : 0);
        etSection.setText(sectionName(editingDrill));
        etMax.setText(String.valueOf(editingDrill.maxNumber));
        etCount.setText(String.valueOf(editingDrill.questionCount));
        etTime.setText(String.valueOf(editingDrill.timeLimitSec > 0 ? editingDrill.timeLimitSec : 20));
        etTitle.setText(editingDrill.title);
        btnSave.setText("Save changes");
        ((TextView) findViewById(R.id.tvCreateTitle)).setText("Edit drill");
    }

    private String sectionName(Drill drill) {
        String section = drill.section == null ? "" : drill.section.trim();
        String prefix = Grade.label(drill.grade) + " - ";
        return section.startsWith(prefix) ? section.substring(prefix.length()).trim() : section;
    }

    private void save() {
        final String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (skills.isEmpty()) {
            UiUtils.showError(tvError, "No skills available for this grade.");
            return;
        }
        Skill skill = skills.get(spSkill.getSelectedItemPosition());
        boolean timed = spMode.getSelectedItemPosition() == 1;

        String error = Validators.section(etSection.getText().toString());
        Integer max = Validators.parseInt(etMax.getText().toString());
        if (error == null && max == null) error = "Largest number must be a whole number.";
        if (error == null) error = DrillGenerator.validateRange(skill, skill.minNumber, max);
        if (error == null) error = Validators.questionCount(etCount.getText().toString());
        if (error == null && timed) error = Validators.timeLimit(etTime.getText().toString());
        if (error == null && etTitle.getText().toString().trim().length() > 80) error = "Drill title is too long (80 characters max).";
        UiUtils.showError(tvError, error);
        if (error != null) return;

        String gradeValue = GRADE_VALUES[spGrade.getSelectedItemPosition()];
        String titleText = etTitle.getText().toString().trim();

        Drill d = new Drill();
        d.teacherUid = uid;
        d.title = titleText.isEmpty() ? skill.title : titleText;
        d.grade = gradeValue;
        d.section = Grade.label(gradeValue) + " - " + etSection.getText().toString().trim();
        d.skillId = skill.id;
        d.topic = skill.topic;
        d.minNumber = skill.minNumber;
        d.maxNumber = max;
        d.questionCount = Validators.parseInt(etCount.getText().toString());
        d.format = spFormat.getSelectedItemPosition() == 1 ? Drill.FORMAT_MULTIPLE_CHOICE : Drill.FORMAT_FLASHCARD;
        d.mode = timed ? Drill.MODE_TIMED : Drill.MODE_UNTIMED;
        d.timeLimitSec = timed ? Validators.parseInt(etTime.getText().toString()) : 0;
        d.createdAt = editingDrill == null ? System.currentTimeMillis() : editingDrill.createdAt;

        if (editingDrill == null) {
            saveNewDrill(d, true);
        } else {
            chooseEditSaveAction(d);
        }
    }

    private void chooseEditSaveAction(Drill drill) {
        String[] options = {"Replace original", "Make a copy", "Cancel"};
        new AlertDialog.Builder(this)
                .setTitle("Save changes")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) replaceDrill(drill);
                    else if (which == 1) saveEditedCopy(drill);
                })
                .show();
    }

    private void replaceDrill(Drill drill) {
        setSaving(true);
        DrillRepository.get().updateDrill(editingDrillId, drill, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) { finish(); }

            @Override
            public void onError(String message) { showSaveError(message); }
        });
    }

    private void saveEditedCopy(Drill drill) {
        drill.title = copyTitle(drill.title);
        drill.createdAt = System.currentTimeMillis();
        saveNewDrill(drill, false);
    }

    private String copyTitle(String title) {
        String prefix = "Copy of ";
        String original = title == null ? "" : title;
        int maxOriginalLength = 80 - prefix.length();
        if (original.length() > maxOriginalLength) original = original.substring(0, maxOriginalLength);
        return prefix + original;
    }

    private void saveNewDrill(Drill drill, boolean startClassMode) {
        setSaving(true);
        DrillRepository.get().saveDrill(drill, new DataCallback<DrillRepository.Entry>() {
            @Override
            public void onSuccess(DrillRepository.Entry entry) {
                if (startClassMode) {
                    Intent i = new Intent(CreateDrillActivity.this, ClassModeActivity.class);
                    i.putExtra(IntentKeys.DRILL, entry.drill);
                    i.putExtra(IntentKeys.DRILL_ID, entry.id);
                    startActivity(i);
                } else {
                    UiUtils.toast(CreateDrillActivity.this, "Drill copied.");
                }
                finish();
            }

            @Override
            public void onError(String message) { showSaveError(message); }
        });
    }

    private void setSaving(boolean saving) {
        UiUtils.show(progress, saving);
        btnSave.setEnabled(!saving);
    }

    private void showSaveError(String message) {
        setSaving(false);
        UiUtils.showError(tvError, message);
    }

    /** Spinner listener that only needs the chosen position. */
    private abstract static class SimpleSelect implements AdapterView.OnItemSelectedListener {
        abstract void onChosen(int position);

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { onChosen(position); }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }
}
