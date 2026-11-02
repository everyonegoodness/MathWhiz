package com.mathwhiz.app.logic;

/** Class-level score and streak for one drill session. */
public class SessionTracker {
    private int total;
    private int correct;
    private int streak;
    private int bestStreak;

    public void record(boolean wasCorrect) {
        total++;
        if (wasCorrect) {
            correct++;
            streak++;
            if (streak > bestStreak) bestStreak = streak;
        } else {
            streak = 0;
        }
    }

    public int getTotal() { return total; }
    public int getCorrect() { return correct; }
    public int getWrong() { return total - correct; }
    public int getStreak() { return streak; }
    public int getBestStreak() { return bestStreak; }

    public int accuracyPercent() {
        return total == 0 ? 0 : (int) Math.round(100.0 * correct / total);
    }
}
