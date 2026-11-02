package com.mathwhiz.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class dashboard: accuracy by topic and skill, most-missed types, session history; filter by grade and section. */
public class DashboardActivity extends AppCompatActivity {
    private static final String[] GRADE_LABELS = {"All grades", "Kindergarten", "Grade 1"};
    private static final String[] GRADE_VALUES = {"", Grade.KINDER, Grade.GRADE_1};
    private static final String[] DATE_RANGE_LABELS = {"All time", "Today", "This month", "Custom range"};
    private static final int DATE_ALL_TIME = 0;
    private static final int DATE_TODAY = 1;
    private static final int DATE_THIS_MONTH = 2;
    private static final int DATE_CUSTOM = 3;

    private Spinner spGrade, spSection, spDateRange;
    private ProgressBar progress;
    private TextView tvError, tvEmpty, tvOverall;
    private Button btnViewAllQuestions, btnViewAllHistory;
    private View content;
    private LinearLayout topicList, skillList, missedList, questionList, historyList;

    private SessionRepository.Data data;
    private List<String> sectionValues = new ArrayList<>();
    private boolean ready;
    private boolean showAllQuestions;
    private boolean showAllHistory;
    private int selectedDateRange = DATE_ALL_TIME;
    private int previousDateRange = DATE_ALL_TIME;
    private long customRangeStart;
    private long customRangeEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        spGrade = findViewById(R.id.spGrade);
        spSection = findViewById(R.id.spSection);
        spDateRange = findViewById(R.id.spDateRange);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvOverall = findViewById(R.id.tvOverall);
        btnViewAllQuestions = findViewById(R.id.btnViewAllQuestions);
        btnViewAllHistory = findViewById(R.id.btnViewAllHistory);
        content = findViewById(R.id.content);
        topicList = findViewById(R.id.topicList);
        skillList = findViewById(R.id.skillList);
        missedList = findViewById(R.id.missedList);
        questionList = findViewById(R.id.questionList);
        historyList = findViewById(R.id.historyList);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());
        btnViewAllQuestions.setOnClickListener(v -> {
            showAllQuestions = !showAllQuestions;
            render();
        });
        btnViewAllHistory.setOnClickListener(v -> {
            showAllHistory = !showAllHistory;
            render();
        });

        ArrayAdapter<String> ga = new ArrayAdapter<>(this, R.layout.item_spinner, GRADE_LABELS);
        ga.setDropDownViewResource(R.layout.item_spinner);
        spGrade.setAdapter(ga);
        ArrayAdapter<String> da = new ArrayAdapter<>(this, R.layout.item_spinner, DATE_RANGE_LABELS);
        da.setDropDownViewResource(R.layout.item_spinner);
        spDateRange.setAdapter(da);
        spDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!ready || position == selectedDateRange) return;
                if (position == DATE_CUSTOM) {
                    showCustomDateRange();
                } else {
                    previousDateRange = selectedDateRange;
                    selectedDateRange = position;
                    render();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ready) {
                    setupSectionFilter();
                    render();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ready) render();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
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
        String selectedGrade = selectedGrade();
        Map<String, String> sections = new LinkedHashMap<>();
        for (SessionResult session : data.sessions) {
            if (session.section == null || session.section.isEmpty()) continue;
            if (!selectedGrade.isEmpty() && !selectedGrade.equals(session.grade)) continue;
            sections.put(session.section, sectionLabel(session));
        }
        sectionValues = new ArrayList<>();
        sectionValues.add("");
        sectionValues.addAll(sections.keySet());
        List<String> labels = new ArrayList<>();
        labels.add("All sections");
        labels.addAll(sections.values());
        ArrayAdapter<String> sa = new ArrayAdapter<>(this, R.layout.item_spinner, labels);
        sa.setDropDownViewResource(R.layout.item_spinner);
        spSection.setAdapter(sa);
    }

    private String selectedGrade() {
        int position = spGrade.getSelectedItemPosition();
        return position >= 0 && position < GRADE_VALUES.length ? GRADE_VALUES[position] : "";
    }

    private String sectionLabel(SessionResult session) {
        String section = session.section.trim();
        String prefix = Grade.label(session.grade) + " - ";
        return section.startsWith(prefix) ? section.substring(prefix.length()).trim() : section;
    }

    private void showCustomDateRange() {
        Calendar today = Calendar.getInstance();
        Calendar initialStart = Calendar.getInstance();
        Calendar initialEnd = Calendar.getInstance();
        if (customRangeEnd > customRangeStart) {
            initialStart.setTimeInMillis(customRangeStart);
            initialEnd.setTimeInMillis(customRangeEnd - 1);
        } else {
            initialStart.set(Calendar.DAY_OF_MONTH, 1);
            initialEnd.setTimeInMillis(today.getTimeInMillis());
        }

        DatePickerDialog startPicker = new DatePickerDialog(this,
                (picker, year, month, day) -> {
                    Calendar start = dayStart(year, month, day);
                    showEndDatePicker(start, initialEnd);
                },
                initialStart.get(Calendar.YEAR), initialStart.get(Calendar.MONTH),
                initialStart.get(Calendar.DAY_OF_MONTH));
        startPicker.setOnCancelListener(dialog -> restoreDateRangeSelection());
        startPicker.show();
    }

    private void showEndDatePicker(Calendar start, Calendar initialEnd) {
        Calendar endSelection = (Calendar) initialEnd.clone();
        if (endSelection.before(start)) endSelection = (Calendar) start.clone();
        DatePickerDialog endPicker = new DatePickerDialog(this,
                (picker, year, month, day) -> {
                    Calendar end = dayStart(year, month, day);
                    Calendar endExclusive = (Calendar) end.clone();
                    endExclusive.add(Calendar.DAY_OF_MONTH, 1);
                    customRangeStart = start.getTimeInMillis();
                    customRangeEnd = endExclusive.getTimeInMillis();
                    previousDateRange = selectedDateRange;
                    selectedDateRange = DATE_CUSTOM;
                    render();
                },
                endSelection.get(Calendar.YEAR), endSelection.get(Calendar.MONTH),
                endSelection.get(Calendar.DAY_OF_MONTH));
        endPicker.getDatePicker().setMinDate(start.getTimeInMillis());
        endPicker.setOnCancelListener(dialog -> restoreDateRangeSelection());
        endPicker.show();
    }

    private Calendar dayStart(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day);
        return calendar;
    }

    private void restoreDateRangeSelection() {
        spDateRange.setSelection(previousDateRange);
    }

    private long[] selectedDateBounds() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        if (selectedDateRange == DATE_TODAY) {
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.DAY_OF_MONTH, 1);
            return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
        }
        if (selectedDateRange == DATE_THIS_MONTH) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.MONTH, 1);
            return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
        }
        if (selectedDateRange == DATE_CUSTOM) return new long[]{customRangeStart, customRangeEnd};
        return new long[]{Long.MIN_VALUE, Long.MAX_VALUE};
    }

    private void render() {
        String grade = selectedGrade();
        int sp = spSection.getSelectedItemPosition();
        String section = sp >= 0 && sp < sectionValues.size() ? sectionValues.get(sp) : "";

        List<SessionResult> sessions = StatsAggregator.filterSessions(data.sessions, grade, section);
        long[] dateBounds = selectedDateBounds();
        sessions = StatsAggregator.filterSessionsByDateRange(sessions, dateBounds[0], dateBounds[1]);
        List<ResponseRecord> responses = StatsAggregator.filterResponsesForSessions(data.responses, sessions);

        boolean empty = sessions.isEmpty() && responses.isEmpty();
        UiUtils.show(tvEmpty, empty);
        UiUtils.show(content, !empty);
        if (data.sessions.isEmpty()) {
            tvEmpty.setText("No sessions yet. Run a drill in class mode to see your class results here.");
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
        List<StatsAggregator.MissedQuestion> mq = StatsAggregator.missedQuestions(responses, responses.size());
        if (mq.isEmpty()) addRow(questionList, "No missed questions.", "", null);
        int questionCount = showAllQuestions ? mq.size() : Math.min(2, mq.size());
        for (int i = 0; i < questionCount; i++) {
            final StatsAggregator.MissedQuestion m = mq.get(i);
            View.OnClickListener click = null;
            if (m.fruit != null && !m.fruit.isEmpty()) click = v -> showFact(m.fruit);
            addRow(questionList, m.question, Topic.label(m.topic) + "  \u2022  missed " + m.count + "x", click);
        }
        updateViewAllButton(btnViewAllQuestions, mq.size(), showAllQuestions);

        historyList.removeAllViews();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        String currentHistoryDate = null;
        int historyCount = showAllHistory ? sessions.size() : Math.min(2, sessions.size());
        for (int i = 0; i < historyCount; i++) {
            SessionResult s = sessions.get(i);
            String sessionDate = dateFormat.format(new Date(s.date));
            if (!sessionDate.equals(currentHistoryDate)) {
                addHistoryDateHeading(sessionDate);
                currentHistoryDate = sessionDate;
            }
            int pct = s.totalQuestions == 0 ? 0 : (int) Math.round(100.0 * s.totalCorrect / s.totalQuestions);
            addRow(historyList, s.title + "  \u2014  " + s.totalCorrect + "/" + s.totalQuestions + " (" + pct + "%)",
                    timeFormat.format(new Date(s.date)) + "  \u2022  " + s.section, null);
        }
        updateViewAllButton(btnViewAllHistory, sessions.size(), showAllHistory);
    }

    private void addHistoryDateHeading(String label) {
        TextView heading = new TextView(this);
        heading.setText(label);
        heading.setTextAppearance(this, R.style.Text_Heading);
        historyList.addView(heading);
    }

    private void updateViewAllButton(Button button, int total, boolean expanded) {
        boolean canExpand = total > 2;
        UiUtils.show(button, canExpand);
        if (canExpand) button.setText(expanded ? "Show less" : "View all (" + total + ")");
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
