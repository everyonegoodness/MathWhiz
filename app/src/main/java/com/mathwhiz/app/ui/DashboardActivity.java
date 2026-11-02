package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.FruitRepository;
import com.mathwhiz.app.data.SessionRepository;
import com.mathwhiz.app.logic.StatsAggregator;
import com.mathwhiz.app.model.FactResult;
import com.mathwhiz.app.model.Grade;
import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;
import com.mathwhiz.app.model.Topic;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Class dashboard: accuracy by topic and skill, most-missed types, session history; filter by grade and section. */
public class DashboardActivity extends AppCompatActivity {
    private static final String[] GRADE_LABELS = {"All grades", "Kindergarten", "Grade 1"};
    private static final String[] GRADE_VALUES = {"", Grade.KINDER, Grade.GRADE_1};

    private Spinner spGrade, spSection;
    private ProgressBar progress;
    private TextView tvError, tvEmpty, tvOverall;
    private View content;
    private LinearLayout topicList, skillList, missedList, questionList, historyList;

    private SessionRepository.Data data;
    private List<String> sectionValues = new ArrayList<>();
    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        spGrade = findViewById(R.id.spGrade);
        spSection = findViewById(R.id.spSection);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvOverall = findViewById(R.id.tvOverall);
        content = findViewById(R.id.content);
        topicList = findViewById(R.id.topicList);
        skillList = findViewById(R.id.skillList);
        missedList = findViewById(R.id.missedList);
        questionList = findViewById(R.id.questionList);
        historyList = findViewById(R.id.historyList);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());

        ArrayAdapter<String> ga = new ArrayAdapter<>(this, R.layout.item_spinner, GRADE_LABELS);
        ga.setDropDownViewResource(R.layout.item_spinner);
        spGrade.setAdapter(ga);
        AdapterView.OnItemSelectedListener rerender = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ready) render();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spGrade.setOnItemSelectedListener(rerender);
        spSection.setOnItemSelectedListener(rerender);
    }

    @Override
    protected void onStart() {
        super.onStart();
        load();
    }

    private void load() {
        String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        ready = false;
        UiUtils.show(progress, true);
        UiUtils.show(content, false);
        UiUtils.show(tvEmpty, false);
        UiUtils.show(findViewById(R.id.btnRetry), false);
        UiUtils.showError(tvError, null);
        SessionRepository.get().loadAll(uid, new DataCallback<SessionRepository.Data>() {
            @Override
            public void onSuccess(SessionRepository.Data value) {
                UiUtils.show(progress, false);
                data = value;
                setupSectionFilter();
                ready = true;
                render();
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                UiUtils.showError(tvError, message);
                UiUtils.show(findViewById(R.id.btnRetry), true);
            }
        });
    }

    private void setupSectionFilter() {
        Set<String> sections = new LinkedHashSet<>();
        for (SessionResult s : data.sessions) if (s.section != null && !s.section.isEmpty()) sections.add(s.section);
        sectionValues = new ArrayList<>();
        sectionValues.add("");
        sectionValues.addAll(sections);
        List<String> labels = new ArrayList<>();
        labels.add("All sections");
        labels.addAll(sections);
        ArrayAdapter<String> sa = new ArrayAdapter<>(this, R.layout.item_spinner, labels);
        sa.setDropDownViewResource(R.layout.item_spinner);
        spSection.setAdapter(sa);
    }

    private void render() {
        String grade = GRADE_VALUES[spGrade.getSelectedItemPosition()];
        int sp = spSection.getSelectedItemPosition();
        String section = sp >= 0 && sp < sectionValues.size() ? sectionValues.get(sp) : "";

        List<SessionResult> sessions = StatsAggregator.filterSessions(data.sessions, grade, section);
        List<ResponseRecord> responses = StatsAggregator.filterResponses(data.responses, grade, section);

        boolean empty = sessions.isEmpty() && responses.isEmpty();
        UiUtils.show(tvEmpty, empty);
        UiUtils.show(content, !empty);
        if (data.sessions.isEmpty()) {
            tvEmpty.setText("No sessions yet. Run a drill in class mode, or tap \"Load demo data\" on the home screen.");
        } else {
            tvEmpty.setText("No sessions match these filters.");
        }
        if (empty) return;

        StatsAggregator.Stat overall = StatsAggregator.overall(responses);
        tvOverall.setText(Math.round(overall.accuracy()) + "% correct  \u2022  " + overall.correct + " of "
                + overall.total + " answers  \u2022  " + sessions.size() + " sessions");

        topicList.removeAllViews();
        for (StatsAggregator.Stat s : StatsAggregator.byTopic(responses)) addStat(topicList, s);
        skillList.removeAllViews();
        for (StatsAggregator.Stat s : StatsAggregator.bySkill(responses)) addStat(skillList, s);

        missedList.removeAllViews();
        List<StatsAggregator.Stat> missed = StatsAggregator.mostMissed(responses, 5);
        if (missed.isEmpty()) addRow(missedList, "Nothing missed yet \uD83C\uDF89", "", null);
        for (StatsAggregator.Stat s : missed) {
            addRow(missedList, s.label, s.wrong() + " wrong out of " + s.total + "  (" + Math.round(s.accuracy()) + "% correct)", null);
        }

        questionList.removeAllViews();
        List<StatsAggregator.MissedQuestion> mq = StatsAggregator.missedQuestions(responses, 8);
        if (mq.isEmpty()) addRow(questionList, "No missed questions.", "", null);
        for (final StatsAggregator.MissedQuestion m : mq) {
            View.OnClickListener click = null;
            if (m.fruit != null && !m.fruit.isEmpty()) click = v -> showFact(m.fruit);
            addRow(questionList, m.question, Topic.label(m.topic) + "  \u2022  missed " + m.count + "x", click);
        }

        historyList.removeAllViews();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        for (SessionResult s : sessions) {
            int pct = s.totalQuestions == 0 ? 0 : (int) Math.round(100.0 * s.totalCorrect / s.totalQuestions);
            addRow(historyList, s.title + "  \u2014  " + s.totalCorrect + "/" + s.totalQuestions + " (" + pct + "%)",
                    df.format(new Date(s.date)) + "  \u2022  " + s.section, null);
        }
    }

    private void addStat(LinearLayout parent, StatsAggregator.Stat s) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_stat_row, parent, false);
        ((TextView) row.findViewById(R.id.tvStatLabel)).setText(s.label);
        ((TextView) row.findViewById(R.id.tvStatValue)).setText(
                Math.round(s.accuracy()) + "%  (" + s.correct + "/" + s.total + ")");
        ((ProgressBar) row.findViewById(R.id.pbStat)).setProgress((int) Math.round(s.accuracy()));
        parent.addView(row);
    }

    private void addRow(LinearLayout parent, String title, String sub, View.OnClickListener click) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_row, parent, false);
        ((TextView) row.findViewById(R.id.tvRowTitle)).setText(title);
        TextView subView = row.findViewById(R.id.tvRowSub);
        subView.setText(sub);
        UiUtils.show(subView, !sub.isEmpty());
        if (click != null) row.setOnClickListener(click);
        parent.addView(row);
    }

    private void showFact(String fruit) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Fruit fun fact")
                .setMessage("Looking up a fact\u2026")
                .setPositiveButton("OK", null)
                .show();
        FruitRepository.get().loadFact(fruit, new DataCallback<FactResult>() {
            @Override
            public void onSuccess(FactResult f) {
                if (isFinishing() || isDestroyed() || !dialog.isShowing()) return;
                dialog.setTitle(f.emoji + "  " + f.fruitName);
                dialog.setMessage(f.text + "\n\n" + (f.fromApi ? "Data from Fruityvice" : "Offline fruit fact"));
            }

            @Override
            public void onError(String message) { }
        });
    }
}
