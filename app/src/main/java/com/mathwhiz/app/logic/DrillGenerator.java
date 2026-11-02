package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.Fruit;
import com.mathwhiz.app.model.Question;
import com.mathwhiz.app.model.Skill;
import com.mathwhiz.app.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Builds fruit-themed questions for a skill and number range.
 * Guarantees: answers are computed here, subtraction never goes below zero, every number stays inside the
 * requested range, and each question has 4 distinct options containing the correct answer exactly once.
 */
public class DrillGenerator {
    private final Random rnd;

    public DrillGenerator(Random rnd) {
        this.rnd = rnd;
    }

    /** Returns an error message if [min,max] is not usable for the skill, otherwise null. */
    public static String validateRange(Skill skill, int min, int max) {
        if (min > max) return "The smallest number cannot be bigger than the largest.";
        if (min < skill.minNumber || max > skill.maxNumber) {
            return "For this skill the largest number must be between " + skill.minNumber + " and "
                    + skill.maxNumber + ".";
        }
        switch (skill.topic) {
            case Topic.COMPARING:
                if (max - min + 1 < 4) return "Comparing needs at least 4 different numbers in the range.";
                break;
            case Topic.ADDITION:
                if (max < 2 * min) return "Addition needs a largest number of at least " + (2 * min) + ".";
                break;
            case Topic.SUBTRACTION:
                if (max < 2) return "Subtraction needs a largest number of at least 2.";
                break;
            default:
                break;
        }
        return null;
    }

    public List<Question> generate(Skill skill, int min, int max, int count, List<Fruit> fruits) {
        String err = validateRange(skill, min, max);
        if (err != null) throw new IllegalArgumentException(err);
        if (count < 1) throw new IllegalArgumentException("Question count must be at least 1.");

        List<Fruit> pool = buildPool(fruits);
        List<Question> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int previousRecognitionNumber = Integer.MIN_VALUE;
        for (int i = 0; i < count; i++) {
            Question q = null;
            for (int attempt = 0; attempt < 30; attempt++) {   // avoid repeats when the range allows it
                q = make(skill, min, max, pool, previousRecognitionNumber);
                if (seen.add(q.prompt + "|" + q.visual + "|" + q.options)) break;
            }
            out.add(q);
            if (Topic.NUMBER_RECOGNITION.equals(skill.topic)) {
                previousRecognitionNumber = Integer.parseInt(q.visual);
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- question builders

    private Question make(Skill s, int min, int max, List<Fruit> pool, int previousRecognitionNumber) {
        switch (s.topic) {
            case Topic.COUNTING: return counting(s, min, max, pick(pool));
            case Topic.NUMBER_RECOGNITION: return numberRecognition(s, min, max, previousRecognitionNumber);
            case Topic.COMPARING: return comparing(s, min, max, pool);
            case Topic.ADDITION: return addition(s, min, max, pick(pool));
            case Topic.SUBTRACTION: return subtraction(s, min, max, pick(pool));
            default: throw new IllegalArgumentException("Unknown topic: " + s.topic);
        }
    }

    private Question counting(Skill s, int min, int max, Fruit f) {
        int n = between(min, max);
        return numeric(s, f.name, "How many " + f.plural() + " do you see?", rows(f.emoji, n), n, 1, s.maxNumber);
    }

    private Question numberRecognition(Skill s, int min, int max, int previousNumber) {
        List<Integer> candidates = new ArrayList<>();
        for (int value = min; value <= max; value++) {
            if (previousNumber == Integer.MIN_VALUE || Math.abs((long) value - previousNumber) > 1) {
                candidates.add(value);
            }
        }
        int n = candidates.isEmpty() ? between(min, max) : candidates.get(rnd.nextInt(candidates.size()));
        List<Integer> wrong = wrongNumbers(n, 1, s.maxNumber, true);
        List<String> opts = new ArrayList<>();
        opts.add(NumberWords.of(n));
        for (int w : wrong) opts.add(NumberWords.of(w));
        return finish(s, "", "What number is this?", String.valueOf(n), opts);
    }

    private Question addition(Skill s, int min, int max, Fruit f) {
        int a = between(min, max - min);
        int b = between(min, max - a);
        String prompt = f.qty(a) + " and " + b + " more. How many " + f.plural() + " in all?";
        String visual = rows(f.emoji, a) + "  +  " + rows(f.emoji, b);
        return numeric(s, f.name, prompt, visual, a + b, 0, Integer.MAX_VALUE);
    }

    private Question subtraction(Skill s, int min, int max, Fruit f) {
        int a = between(Math.max(min, 2), max);
        int b = between(1, a);                       // b <= a, so the answer is never below zero
        String prompt = "There are " + f.qty(a) + ". " + b + (b == 1 ? " is" : " are") + " eaten. How many "
                + f.plural() + " are left?";
        String visual = rows(f.emoji, a) + "  \u2212  " + rows(f.emoji, b);
        return numeric(s, f.name, prompt, visual, a - b, 0, Integer.MAX_VALUE);
    }

    private Question comparing(Skill s, int min, int max, List<Fruit> pool) {
        boolean most = rnd.nextBoolean();
        List<Integer> range = new ArrayList<>();
        for (int v = min; v <= max; v++) range.add(v);
        Collections.shuffle(range, rnd);
        List<Integer> nums = new ArrayList<>(range.subList(0, 4));   // 4 DIFFERENT numbers

        int best = 0;
        for (int i = 1; i < 4; i++) {
            if (most ? nums.get(i) > nums.get(best) : nums.get(i) < nums.get(best)) best = i;
        }

        List<String> options = new ArrayList<>();
        StringBuilder visual = new StringBuilder();
        String fruitName = "";
        if (Skill.STYLE_GROUPS.equals(s.style)) {
            List<Fruit> four = pickDistinct(pool, 4);
            for (int i = 0; i < 4; i++) {
                String group = rows(four.get(i).emoji, nums.get(i));
                options.add(group);
                if (i > 0) visual.append('\n');
                visual.append(Question.letter(i)).append("   ").append(group);
            }
            fruitName = four.get(best).name;
        } else {
            for (int i = 0; i < 4; i++) {
                options.add(String.valueOf(nums.get(i)));
                if (i > 0) visual.append("     ");
                visual.append(nums.get(i));
            }
        }
        String prompt = Skill.STYLE_GROUPS.equals(s.style)
                ? "Which group has the " + (most ? "MOST" : "FEWEST") + " fruit?"
                : "Which number is the " + (most ? "BIGGEST" : "SMALLEST") + "?";
        String answer = Question.letter(best) + "   " + options.get(best);
        return new Question(s.id, s.topic, fruitName, prompt, visual.toString(), answer, options, best,
                Skill.STYLE_GROUPS.equals(s.style));
    }

    // ---------------------------------------------------------------- helpers

    private Question numeric(Skill s, String fruit, String prompt, String visual, int correct, int lb, int ub) {
        List<String> opts = new ArrayList<>();
        opts.add(String.valueOf(correct));
        for (int w : wrongNumbers(correct, lb, ub, false)) opts.add(String.valueOf(w));
        return finish(s, fruit, prompt, visual, opts);
    }

    /** opts.get(0) must be the correct option; shuffles and records where it ended up. */
    private Question finish(Skill s, String fruit, String prompt, String visual, List<String> opts) {
        String correct = opts.get(0);
        Collections.shuffle(opts, rnd);
        return new Question(s.id, s.topic, fruit, prompt, visual, correct, opts, opts.indexOf(correct), false);
    }

    /** Three distinct, plausible wrong numbers (near the answer; digit-swap for 2-digit numerals). */
    List<Integer> wrongNumbers(int correct, int lb, int ub, boolean allowReversal) {
        List<Integer> picked = collectWrong(correct, lb, ub, allowReversal);
        if (picked.size() < 3) picked = collectWrong(correct, lb, Integer.MAX_VALUE, allowReversal); // relax upper bound
        return picked;
    }

    private List<Integer> collectWrong(int correct, int lb, int ub, boolean allowReversal) {
        Set<Integer> cands = new LinkedHashSet<>();
        int[] offsets = {1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 10, -10, 6, 7, 8, 9, 11, 12};
        if (allowReversal && correct >= 10 && correct < 100) {
            cands.add((correct % 10) * 10 + correct / 10);
        }
        for (int o : offsets) cands.add(correct + o);

        List<Integer> valid = new ArrayList<>();
        for (int c : cands) {
            if (c != correct && c >= lb && c <= ub) valid.add(c);
        }
        List<Integer> nearest = new ArrayList<>(valid.subList(0, Math.min(5, valid.size())));
        Collections.shuffle(nearest, rnd);
        List<Integer> out = new ArrayList<>(nearest.subList(0, Math.min(3, nearest.size())));
        // if fewer than 3 near candidates, top up from the rest of the valid list
        for (int v : valid) {
            if (out.size() >= 3) break;
            if (!out.contains(v)) out.add(v);
        }
        return out;
    }

    private int between(int lo, int hi) {
        if (hi < lo) throw new IllegalArgumentException("Empty range " + lo + ".." + hi);
        return lo + rnd.nextInt(hi - lo + 1);
    }

    private Fruit pick(List<Fruit> pool) {
        return pool.get(rnd.nextInt(pool.size()));
    }

    private List<Fruit> pickDistinct(List<Fruit> pool, int n) {
        List<Fruit> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, rnd);
        return new ArrayList<>(copy.subList(0, n));
    }

    /** Pool of usable fruits; always at least 4 distinct so "which group" questions work. */
    static List<Fruit> buildPool(List<Fruit> given) {
        Map<String, Fruit> map = new LinkedHashMap<>();
        if (given != null) {
            for (Fruit f : given) {
                if (f != null && f.emoji != null && !f.emoji.isEmpty()) map.put(f.name, f);
            }
        }
        if (map.size() < 4) {
            for (Fruit f : FruitCatalog.fallbackFruits()) {
                if (!map.containsKey(f.name)) map.put(f.name, f);
                if (map.size() >= 8) break;
            }
        }
        return new ArrayList<>(map.values());
    }

    /** Emoji picture row: a gap every 5, a new line every 10. */
    public static String rows(String emoji, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            sb.append(emoji);
            if (i < n) {
                if (i % 10 == 0) sb.append('\n');
                else if (i % 5 == 0) sb.append(' ');
            }
        }
        return sb.toString();
    }
}
