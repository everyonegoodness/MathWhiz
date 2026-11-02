package com.mathwhiz.app.model;

import java.util.List;

/** One generated question. Always carries 4 options so the same drill can be played as flashcard or multiple choice. */
public final class Question {
    public final String skillId;
    public final String topic;
    public final String fruit;        // fruit name used (may be "" for numeral questions)
    public final String prompt;       // text read aloud
    public final String visual;       // big picture area: emoji rows / numeral
    public final String answer;       // text shown on Reveal / feedback
    public final List<String> options;
    public final int correctIndex;
    public final boolean optionsAreVisual; // comparing groups: the options ARE the picture

    public Question(String skillId, String topic, String fruit, String prompt, String visual,
                    String answer, List<String> options, int correctIndex, boolean optionsAreVisual) {
        this.skillId = skillId;
        this.topic = topic;
        this.fruit = fruit;
        this.prompt = prompt;
        this.visual = visual;
        this.answer = answer;
        this.options = options;
        this.correctIndex = correctIndex;
        this.optionsAreVisual = optionsAreVisual;
    }

    public static String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }
}
