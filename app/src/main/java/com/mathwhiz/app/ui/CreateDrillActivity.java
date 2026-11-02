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
    private static final String[] FORMAT_LABELS = {"Flashcard (Reveal, then Correct / Wrong)", "Multiple choice (tap the called option)"};
    private static final String[] MODE_LABELS = {"Untimed", "Timed"};

    private Spinner spGrade, spSkill, spFormat, spMode;
    private EditText etSection, etMax, etCount, etTime, etTitle;
    private TextView tvSkillInfo, tvError;
    private View timeRow;
    private Button btnSave;
    private ProgressBar progress;
    private final List<Skill> skills = new ArrayList<>();

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
        tvSkillInfo = findViewById(R.id.tvSkillInfo);
        tvError = findViewById(R.id.tvError);
        timeRow = findViewById(R.id.timeRow);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        spGrade.setAdapter(adapter(GRADE_LABELS));
        spFormat.setAdapter(adapter(FORMAT_LABELS));
        spMode.setAdapter(adapter(MODE_LABELS));
        etCount.setText("10");
        etTime.setText("20");

        spGrade.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            void onChosen(int position) { loadSkillsFor(GRADE_VALUES[position]); }
        });
        spSkill.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            void onChosen(int position) { showSkill(skills.get(position)); }
        });
        spMode.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            void onChosen(int position) { UiUtils.show(timeRow, position == 1); }
        });
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
        for (Skill s : skills) titles.add(Topic.label(s.topic) + ": " + s.title);
        spSkill.setAdapter(adapter(titles.toArray(new String[0])));
        if (!skills.isEmpty()) showSkill(skills.get(0));
    }

    private void showSkill(Skill s) {
        tvSkillInfo.setText("Numbers " + s.minNumber + " to " + s.maxNumber + "\nSource: " + s.sourceRef);
        etMax.setText(String.valueOf(s.maxNumber));
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
        d.createdAt = System.currentTimeMillis();

        UiUtils.show(progress, true);
        btnSave.setEnabled(false);
        final android.content.Context appCtx = getApplicationContext();
        DrillRepository.get().saveDrill(d, new DataCallback<DrillRepository.Entry>() {
            @Override
            public void onSuccess(DrillRepository.Entry entry) {
                Intent i = new Intent(CreateDrillActivity.this, ClassModeActivity.class);
                i.putExtra(IntentKeys.DRILL, entry.drill);
                i.putExtra(IntentKeys.DRILL_ID, entry.id);
                startActivity(i);
                finish();
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                btnSave.setEnabled(true);
                UiUtils.showError(tvError, message);
            }
        }, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) { }

            @Override
            public void onError(String message) { UiUtils.toast(appCtx, message); }
        });
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
