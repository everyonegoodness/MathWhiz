package com.mathwhiz.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.FruitRepository;
import com.mathwhiz.app.data.SessionRepository;
import com.mathwhiz.app.logic.CurriculumGuide;
import com.mathwhiz.app.logic.DrillGenerator;
import com.mathwhiz.app.logic.SessionTracker;
import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.FruitsResult;
import com.mathwhiz.app.model.Question;
import com.mathwhiz.app.model.Topic;
import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;
import com.mathwhiz.app.model.Skill;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Landscape classroom screen for the TV/projector. The class answers aloud; the teacher taps.
 * Flashcard: Reveal -> Correct/Wrong -> Next.   Multiple choice: tap the called option -> Next.
 */
public class ClassModeActivity extends AppCompatActivity {
    private enum State { LOADING, READY, ASKING, REVEALED, ANSWERED }

    private Drill drill;
    private String drillId;
    private Skill skill;
    private List<Question> questions = new ArrayList<>();
    private final SessionTracker tracker = new SessionTracker();
    private final List<ResponseRecord> responses = new ArrayList<>();
    private final Set<String> missedFruits = new LinkedHashSet<>();
    private final Set<String> usedFruits = new LinkedHashSet<>();
    private State state = State.LOADING;
    private int index;
    private int selectedOptionIndex = -1;
    private long questionStartMs;
    private long remainingMs;
    private CountDownTimer timer;

    private View startPanel, feedbackPanel, rowMark, mcGrid;
    private ScrollView questionScroll;
    private TextView tvStartTitle, tvStartInfo, tvFruitStatus;
    private TextView tvTeacherMode;
    private TextView tvProgress, tvStreak, tvTimer, tvScore, tvPrompt, tvVisual, tvAnswer, tvFeedback;
    private Button btnStart, btnReveal, btnCorrect, btnWrong, btnNext;
    private Button[] optionButtons;
    private ProgressBar startProgress;
    private final PresentationCoordinator.StatusListener displayStatusListener = available -> {
        if (tvTeacherMode != null) {
            UiUtils.show(tvTeacherMode, available);
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drill = (Drill) getIntent().getSerializableExtra(IntentKeys.DRILL);
        drillId = getIntent().getStringExtra(IntentKeys.DRILL_ID);
        skill = drill == null ? null : CurriculumGuide.byId(drill.skillId);
        if (skill == null) {
            UiUtils.toast(this, "This drill could not be opened.");
            finish();
            return;
        }
        setContentView(R.layout.activity_class_mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindViews();
        PresentationCoordinator.get().showWaiting();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { confirmExit(); }
        });

        tvStartTitle.setText(DrillTitle.display(drill));
        tvStartInfo.setText(drill.section);
        btnStart.setEnabled(false);
        btnStart.setOnClickListener(v -> {
            UiUtils.show(startPanel, false);
            showQuestion(0);
        });
        findViewById(R.id.btnStartBack).setOnClickListener(v -> finish());
        loadQuestions();
    }

    private void bindViews() {
        startPanel = findViewById(R.id.startPanel);
        tvTeacherMode = findViewById(R.id.tvTeacherMode);
        feedbackPanel = findViewById(R.id.feedbackPanel);
        rowMark = findViewById(R.id.rowMark);
        mcGrid = findViewById(R.id.mcGrid);
        questionScroll = findViewById(R.id.questionScroll);
        tvStartTitle = findViewById(R.id.tvStartTitle);
        tvStartInfo = findViewById(R.id.tvStartInfo);
        tvFruitStatus = findViewById(R.id.tvFruitStatus);
        startProgress = findViewById(R.id.startProgress);
        tvProgress = findViewById(R.id.tvProgress);
        tvStreak = findViewById(R.id.tvStreak);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvVisual = findViewById(R.id.tvVisual);
        tvAnswer = findViewById(R.id.tvAnswer);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnStart = findViewById(R.id.btnStart);
        btnReveal = findViewById(R.id.btnReveal);
        btnCorrect = findViewById(R.id.btnCorrect);
        btnWrong = findViewById(R.id.btnWrong);
        btnNext = findViewById(R.id.btnNext);
        optionButtons = new Button[]{findViewById(R.id.btnOptA), findViewById(R.id.btnOptB),
                findViewById(R.id.btnOptC), findViewById(R.id.btnOptD)};

        btnReveal.setOnClickListener(v -> onReveal());
        btnCorrect.setOnClickListener(v -> onMark(true));
        btnWrong.setOnClickListener(v -> onMark(false));
        btnNext.setOnClickListener(v -> onNext());
        for (int i = 0; i < optionButtons.length; i++) {
            final int choice = i;
            optionButtons[i].setOnClickListener(v -> onOption(choice));
        }
    }

    // ------------------------------------------------------------------ loading

    private void loadQuestions() {
        tvFruitStatus.setText("Getting fruit pictures\u2026");
        UiUtils.show(startProgress, true);
        FruitRepository.get().loadFruits(new DataCallback<FruitsResult>() {
            @Override
            public void onSuccess(FruitsResult result) {
                if (isFinishing() || isDestroyed()) return;
                UiUtils.show(startProgress, false);
                try {
                    questions = new DrillGenerator(new Random())
                            .generate(skill, drill.minNumber, drill.maxNumber, drill.questionCount, result.fruits);
                } catch (IllegalArgumentException e) {
                    tvFruitStatus.setText("This drill has settings that cannot make questions: " + e.getMessage());
                    return;
                }
                tvFruitStatus.setText(result.fromApi
                        ? "Fruits loaded from Fruityvice"
                        : "Using the built-in fruit list (Fruityvice could not be reached)");
                state = State.READY;
                btnStart.setEnabled(true);
            }

            @Override
            public void onError(String message) { }
        });
    }

    // ------------------------------------------------------------------ question flow

    private void showQuestion(int i) {
        index = i;
        selectedOptionIndex = -1;
        questionScroll.post(() -> questionScroll.scrollTo(0, 0));
        Question q = questions.get(i);
        boolean mc = drill.formatIsMultipleChoice();

        tvProgress.setText("Question " + (i + 1) + " of " + questions.size());
        tvPrompt.setText(q.prompt);
        tvVisual.setText(q.visual);
        UiUtils.show(tvVisual, !(mc && q.optionsAreVisual));   // comparing groups: the options ARE the picture
        UiUtils.show(tvAnswer, false);
        UiUtils.show(feedbackPanel, false);
        UiUtils.show(btnNext, false);

        if (mc) {
            UiUtils.show(mcGrid, true);
            UiUtils.show(btnReveal, false);
            UiUtils.show(rowMark, false);
            int blue = ContextCompat.getColor(this, R.color.mw_blue);
            for (int k = 0; k < optionButtons.length; k++) {
                optionButtons[k].setText(Question.letter(k) + "   " + q.options.get(k));
                optionButtons[k].setBackgroundTintList(ColorStateList.valueOf(blue));
            }
        } else {
            UiUtils.show(mcGrid, false);
            UiUtils.show(btnReveal, true);
            UiUtils.show(rowMark, false);
        }
        updateStatus();
        state = State.ASKING;
        questionStartMs = System.currentTimeMillis();
        if (drill.modeIsTimed()) {
            remainingMs = drill.timeLimitSec * 1000L;
            UiUtils.show(tvTimer, true);
            startTimer(remainingMs);
        } else {
            UiUtils.show(tvTimer, false);
        }
        PresentationCoordinator.get().showQuestion(drill, q, i, questions.size());
    }

    private void onReveal() {
        if (state != State.ASKING) return;
        tvAnswer.setText("Answer: " + questions.get(index).answer);
        UiUtils.show(tvAnswer, true);
        UiUtils.show(btnReveal, false);
        UiUtils.show(rowMark, true);
        state = State.REVEALED;
        PresentationCoordinator.get().showAnswer();
    }

    private void onMark(boolean correct) {
        if (state != State.REVEALED) return;
        record(correct, "", false);
        showFeedback(correct, false);
    }

    private void onOption(int choice) {
        if (state != State.ASKING) return;
        Question q = questions.get(index);
        boolean correct = choice == q.correctIndex;
        int green = ContextCompat.getColor(this, R.color.mw_green);
        int red = ContextCompat.getColor(this, R.color.mw_red);
        optionButtons[q.correctIndex].setBackgroundTintList(ColorStateList.valueOf(green));   // always show the answer
        if (!correct) optionButtons[choice].setBackgroundTintList(ColorStateList.valueOf(red));
        selectedOptionIndex = choice;
        record(correct, Question.letter(choice) + "   " + q.options.get(choice), false);
        showFeedback(correct, false);
    }

    private void onTimeUp() {
        if (state != State.ASKING && state != State.REVEALED) return;
        Question q = questions.get(index);
        if (drill.formatIsMultipleChoice()) {
            optionButtons[q.correctIndex].setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mw_green)));
        }
        record(false, "", true);
        showFeedback(false, true);
    }

    private void record(boolean correct, String chosen, boolean timedOut) {
        cancelTimer();
        Question q = questions.get(index);
        tracker.record(correct);
        if (!q.fruit.isEmpty()) {
            usedFruits.add(q.fruit);
            if (!correct) missedFruits.add(q.fruit);
        }

        ResponseRecord r = new ResponseRecord();
        r.teacherUid = drill.teacherUid;
        r.grade = drill.grade;
        r.section = drill.section;
        r.skillId = q.skillId;
        r.topic = q.topic;
        r.fruit = q.fruit;
        r.question = (q.prompt + "  " + q.visual).replace("\n", " ").trim();
        r.correctAnswer = q.answer;
        r.chosenOption = chosen;
        r.correct = correct;
        r.timedOut = timedOut;
        r.timeSec = (System.currentTimeMillis() - questionStartMs) / 1000.0;
        r.timestamp = System.currentTimeMillis();
        responses.add(r);
    }

    /** Right/wrong feedback uses an icon AND a color, so it is readable without relying on color alone. */
    private void showFeedback(boolean correct, boolean timedOut) {
        Question q = questions.get(index);
        feedbackPanel.setBackgroundResource(correct ? R.drawable.bg_feedback_correct : R.drawable.bg_feedback_wrong);
        tvFeedback.setText(timedOut ? "Time's up!" : correct ? "Correct!" : "Not quite");
        tvFeedback.setCompoundDrawablesRelativeWithIntrinsicBounds(
                correct ? R.drawable.ic_check : R.drawable.ic_cross, 0, 0, 0);
        UiUtils.show(feedbackPanel, true);
        tvAnswer.setText("Answer: " + q.answer);
        UiUtils.show(tvAnswer, true);
        UiUtils.show(btnReveal, false);
        UiUtils.show(rowMark, false);
        boolean last = index + 1 >= questions.size();
        btnNext.setText(last ? R.string.see_results : R.string.next_question);
        UiUtils.show(btnNext, true);
        state = State.ANSWERED;
        updateStatus();
        PresentationCoordinator.get().showFeedback(correct,
                getString(timedOut ? R.string.classroom_times_up
                        : correct ? R.string.classroom_correct : R.string.classroom_incorrect),
                drill.formatIsMultipleChoice() ? q.correctIndex : -1,
                selectedOptionIndex);
    }

    private void onNext() {
        if (state != State.ANSWERED) return;
        if (index + 1 < questions.size()) showQuestion(index + 1);
        else finishDrill();
    }

    private void updateStatus() {
        tvStreak.setText("🔥 Streak: " + tracker.getStreak());
        tvScore.setText("\u2714 " + tracker.getCorrect() + "   \u2716 " + tracker.getWrong());
    }

    // ------------------------------------------------------------------ timer

    private void startTimer(long ms) {
        cancelTimer();
        timer = new CountDownTimer(ms, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                long secs = (millisUntilFinished + 999) / 1000;
                tvTimer.setText("\u23F1 " + secs + " s");
                tvTimer.setTextColor(ContextCompat.getColor(ClassModeActivity.this,
                        secs <= 5 ? R.color.mw_red : R.color.mw_ink));
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                tvTimer.setText("\u23F1 0 s");
                onTimeUp();
            }
        }.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PresentationCoordinator.get().setStatusListener(displayStatusListener);
        // Resume the countdown after the screen was in the background.
        if (drill != null && drill.modeIsTimed() && (state == State.ASKING || state == State.REVEALED) && remainingMs > 0) {
            startTimer(remainingMs);
        }
    }

    @Override
    protected void onStop() {
        PresentationCoordinator.get().clearStatusListener(displayStatusListener);
        super.onStop();
        cancelTimer();
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) PresentationCoordinator.get().showWaiting();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ finishing

    private void confirmExit() {
        if (state == State.LOADING || state == State.READY) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("End this drill?")
                .setMessage("The results so far will NOT be saved.")
                .setNegativeButton("Keep going", null)
                .setPositiveButton("End drill", (d, w) -> finish())
                .show();
    }

    private void finishDrill() {
        cancelTimer();
        PresentationCoordinator.get().showWaiting();
        String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            UiUtils.toast(this, "You were signed out, so the results could not be saved.");
            finish();
            return;
        }
        SessionResult s = new SessionResult();
        s.teacherUid = uid;
        s.drillId = drillId == null ? "" : drillId;
        s.title = drill.title;
        s.grade = drill.grade;
        s.section = drill.section;
        s.skillId = drill.skillId;
        s.topic = drill.topic;
        s.format = drill.format;
        s.mode = drill.mode;
        s.date = System.currentTimeMillis();
        s.totalCorrect = tracker.getCorrect();
        s.totalQuestions = tracker.getTotal();
        s.bestStreak = tracker.getBestStreak();

        final Context appCtx = getApplicationContext();
        SessionRepository.get().saveSession(s, responses, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) { }

            @Override
            public void onError(String message) { UiUtils.toast(appCtx, message); }
        });

        Intent i = new Intent(this, ResultsActivity.class);
        i.putExtra(IntentKeys.DRILL, drill);
        i.putExtra(IntentKeys.DRILL_ID, drillId);
        i.putExtra(IntentKeys.TOTAL, tracker.getTotal());
        i.putExtra(IntentKeys.CORRECT, tracker.getCorrect());
        i.putExtra(IntentKeys.BEST_STREAK, tracker.getBestStreak());
        i.putStringArrayListExtra(IntentKeys.MISSED_FRUITS, new ArrayList<>(missedFruits));
        i.putStringArrayListExtra(IntentKeys.USED_FRUITS, new ArrayList<>(usedFruits));
        startActivity(i);
        finish();
    }
}
