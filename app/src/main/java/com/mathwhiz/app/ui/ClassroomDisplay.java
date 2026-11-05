package com.mathwhiz.app.ui;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mathwhiz.app.R;
import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.Question;
import com.mathwhiz.app.model.Topic;

/** Read-only classroom view shown on an Android presentation display. */
final class ClassroomDisplay extends Presentation {
    private View waitingContent;
    private View drillContent;
    private TextView title;
    private TextView progress;
    private TextView section;
    private TextView prompt;
    private TextView visual;
    private TextView answer;
    private TextView feedback;
    private LinearLayout options;

    ClassroomDisplay(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_classroom);
        waitingContent = findViewById(R.id.classroomWaitingContent);
        drillContent = findViewById(R.id.classroomDrillContent);
        title = findViewById(R.id.tvClassroomBrand);
        progress = findViewById(R.id.tvClassroomProgress);
        section = findViewById(R.id.tvClassroomSection);
        prompt = findViewById(R.id.tvClassroomPrompt);
        visual = findViewById(R.id.tvClassroomVisual);
        answer = findViewById(R.id.tvClassroomAnswer);
        feedback = findViewById(R.id.tvClassroomFeedback);
        options = findViewById(R.id.classroomOptions);
        showWaiting();
    }

    void showWaiting() {
        waitingContent.setVisibility(View.VISIBLE);
        drillContent.setVisibility(View.GONE);
        options.removeAllViews();
    }

    void showQuestion(Drill drill, Question question, int index, int count) {
        waitingContent.setVisibility(View.GONE);
        drillContent.setVisibility(View.VISIBLE);
        title.setText(DrillTitle.display(drill));
        section.setText(drill.section);
        progress.setText(getContext().getString(R.string.classroom_progress, index + 1, count));
        prompt.setText(question.prompt);
        visual.setText(question.visual);
        visual.setVisibility(drill.formatIsMultipleChoice() && question.optionsAreVisual
                ? View.GONE : View.VISIBLE);
        answer.setVisibility(View.GONE);
        feedback.setVisibility(View.GONE);
        options.removeAllViews();

        if (drill.formatIsMultipleChoice()) {
            for (int i = 0; i < question.options.size(); i++) {
                TextView option = createOptionView();
                option.setText(Question.letter(i) + "   " + question.options.get(i));
                options.addView(option);
            }
        }
    }

    void showAnswer(String value) {
        answer.setText(getContext().getString(R.string.classroom_answer, value));
        answer.setVisibility(View.VISIBLE);
    }

    void showFeedback(String value, String feedbackText, boolean correct,
                      int correctOption, int selectedOption) {
        showAnswer(value);
        feedback.setText(feedbackText);
        feedback.setTextColor(getContext().getColor(
                correct ? R.color.mw_green_dark : R.color.mw_red));
        feedback.setVisibility(View.VISIBLE);

        for (int i = 0; i < options.getChildCount(); i++) {
            TextView option = (TextView) options.getChildAt(i);
            if (i == correctOption) {
                option.setBackgroundResource(R.drawable.bg_feedback_correct);
                option.setTextColor(getContext().getColor(R.color.mw_white));
            } else if (i == selectedOption) {
                option.setBackgroundResource(R.drawable.bg_feedback_wrong);
                option.setTextColor(getContext().getColor(R.color.mw_white));
            }
        }
    }

    private TextView createOptionView() {
        TextView option = new TextView(getContext());
        option.setTextAppearance(R.style.Text_Question);
        option.setTextColor(getContext().getColor(R.color.mw_ink));
        option.setTextSize(22f);
        option.setPadding(dp(18), dp(12), dp(18), dp(12));
        option.setBackgroundResource(R.drawable.bg_login_card);
        option.setClickable(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        option.setLayoutParams(params);
        return option;
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }
}
