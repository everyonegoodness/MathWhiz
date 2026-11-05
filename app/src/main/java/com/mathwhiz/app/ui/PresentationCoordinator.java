package com.mathwhiz.app.ui;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.Question;

/** Keeps classroom displays waiting until a teacher starts a drill and restores waiting afterwards. */
public final class PresentationCoordinator implements DisplayManager.DisplayListener {
    private static final String TAG = "PresentationCoordinator";

    public interface StatusListener {
        void onPresentationAvailabilityChanged(boolean available);
    }

    private static PresentationCoordinator instance;

    private final Context appContext;
    private final DisplayManager displayManager;
    private ClassroomDisplay presentation;
    private Display presentationDisplay;
    private Drill currentDrill;
    private Question currentQuestion;
    private int questionIndex;
    private int questionCount;
    private boolean answerRevealed;
    private boolean feedbackVisible;
    private boolean feedbackCorrect;
    private String feedbackText;
    private int correctOption = -1;
    private int selectedOption = -1;
    private StatusListener statusListener;

    private PresentationCoordinator(Context context) {
        appContext = context.getApplicationContext();
        displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(this, new Handler(Looper.getMainLooper()));
        updatePresentationDisplay();
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new PresentationCoordinator(context.getApplicationContext());
        }
    }

    public static PresentationCoordinator get() {
        if (instance == null) {
            throw new IllegalStateException("PresentationCoordinator has not been initialized.");
        }
        return instance;
    }

    public void setStatusListener(StatusListener listener) {
        statusListener = listener;
        if (listener != null) listener.onPresentationAvailabilityChanged(isAvailable());
    }

    public void clearStatusListener(StatusListener listener) {
        if (statusListener == listener) statusListener = null;
    }

    public boolean isAvailable() {
        return presentation != null && presentation.isShowing();
    }

    public void showWaiting() {
        currentDrill = null;
        currentQuestion = null;
        answerRevealed = false;
        feedbackVisible = false;
        feedbackText = null;
        feedbackCorrect = false;
        correctOption = -1;
        selectedOption = -1;
        render();
    }

    public void showQuestion(Drill drill, Question question, int index, int count) {
        currentDrill = drill;
        currentQuestion = question;
        questionIndex = index;
        questionCount = count;
        answerRevealed = false;
        feedbackVisible = false;
        feedbackText = null;
        feedbackCorrect = false;
        correctOption = -1;
        selectedOption = -1;
        render();
    }

    public void showAnswer() {
        if (currentDrill == null || currentQuestion == null) return;
        answerRevealed = true;
        render();
    }

    public void showFeedback(boolean wasCorrect, int answerOption, int chosenOption) {
        showFeedback(wasCorrect, wasCorrect ? "Correct!" : "Not quite",
                answerOption, chosenOption);
    }

    public void showFeedback(boolean wasCorrect, String resultText,
                             int answerOption, int chosenOption) {
        if (currentDrill == null || currentQuestion == null) return;
        answerRevealed = true;
        feedbackVisible = true;
        feedbackCorrect = wasCorrect;
        feedbackText = resultText;
        correctOption = answerOption;
        selectedOption = chosenOption;
        render();
    }

    private void updatePresentationDisplay() {
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        Display nextDisplay = displays.length == 0 ? null : displays[0];
        if (presentationDisplay != null && nextDisplay != null
                && presentationDisplay.getDisplayId() == nextDisplay.getDisplayId()) return;

        dismissPresentation();
        presentationDisplay = nextDisplay;
        if (presentationDisplay != null) {
            ClassroomDisplay nextPresentation = new ClassroomDisplay(appContext, presentationDisplay);
            try {
                nextPresentation.show();
                presentation = nextPresentation;
                notifyAvailabilityChanged();
                render();
            } catch (WindowManager.InvalidDisplayException e) {
                presentationDisplay = null;
                nextPresentation.dismiss();
                Log.w(TAG, "The classroom display disconnected before its presentation could open.", e);
                notifyAvailabilityChanged();
            }
        } else {
            notifyAvailabilityChanged();
        }
    }

    private void dismissPresentation() {
        if (presentation != null) {
            presentation.dismiss();
            presentation = null;
        }
        presentationDisplay = null;
    }

    private void render() {
        if (presentation == null || !presentation.isShowing()) return;
        if (currentDrill == null || currentQuestion == null) {
            presentation.showWaiting();
        } else {
            presentation.showQuestion(currentDrill, currentQuestion, questionIndex, questionCount);
            if (feedbackVisible) {
                presentation.showFeedback(currentQuestion.answer, feedbackText, feedbackCorrect,
                        correctOption, selectedOption);
            } else if (answerRevealed) {
                presentation.showAnswer(currentQuestion.answer);
            }
        }
    }

    private void notifyAvailabilityChanged() {
        if (statusListener != null) statusListener.onPresentationAvailabilityChanged(isAvailable());
    }

    @Override
    public void onDisplayAdded(int displayId) {
        updatePresentationDisplay();
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        updatePresentationDisplay();
    }

    @Override
    public void onDisplayChanged(int displayId) {
        updatePresentationDisplay();
    }
}
