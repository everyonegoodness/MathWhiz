package com.mathwhiz.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mathwhiz.app.logic.CurriculumGuide;
import com.mathwhiz.app.logic.DrillGenerator;
import com.mathwhiz.app.logic.FruitCatalog;
import com.mathwhiz.app.logic.NumberWords;
import com.mathwhiz.app.model.Fruit;
import com.mathwhiz.app.model.Question;
import com.mathwhiz.app.model.Skill;
import com.mathwhiz.app.model.Topic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DrillGeneratorTest {

    private static List<Fruit> fruits() { return FruitCatalog.fallbackFruits(); }

    private static int count(String text, String emoji) {
        int c = 0, i = 0;
        while ((i = text.indexOf(emoji, i)) >= 0) { c++; i += emoji.length(); }
        return c;
    }

    private static List<Integer> numbersIn(String s) {
        List<Integer> out = new ArrayList<>();
        for (String part : s.trim().split("\\s+")) out.add(Integer.parseInt(part));
        return out;
    }

    /** Structural rules that must hold for EVERY question of EVERY skill. */
    @Test
    public void everySkillProducesValidQuestions() {
        for (Skill skill : CurriculumGuide.all()) {
            for (long seed = 1; seed <= 40; seed++) {
                DrillGenerator gen = new DrillGenerator(new Random(seed));
                List<Question> qs = gen.generate(skill, skill.minNumber, skill.maxNumber, 30, fruits());
                assertEquals(30, qs.size());
                for (Question q : qs) {
                    assertEquals(skill.id, q.skillId);
                    assertEquals(4, q.options.size());
                    assertEquals("options must be distinct: " + q.options, 4, new HashSet<>(q.options).size());
                    assertTrue(q.correctIndex >= 0 && q.correctIndex < 4);
                    String correctOption = q.options.get(q.correctIndex);
                    int occurrences = 0;
                    for (String o : q.options) if (o.equals(correctOption)) occurrences++;
                    assertEquals(1, occurrences);
                    assertTrue("answer text must contain the correct option", q.answer.contains(correctOption));
                    assertFalse(q.prompt.isEmpty());
                }
            }
        }
    }

    @Test
    public void additionAnswersAreCorrectAndWithinRange() {
        Skill skill = CurriculumGuide.byId("K-ADD-1");           // sums up to 5
        for (long seed = 1; seed <= 200; seed++) {
            for (Question q : new DrillGenerator(new Random(seed)).generate(skill, 1, 5, 10, fruits())) {
                String[] sides = q.visual.split("\\s\\+\\s");   // "ðŸŽðŸŽ  +  ðŸŽ"
                Fruit f = null;
                for (Fruit fr : fruits()) if (q.visual.contains(fr.emoji)) f = fr;
                assertNotNull(f);
                int a = count(sides[0], f.emoji), b = count(sides[1], f.emoji);
                assertTrue(a >= 1 && b >= 1);
                assertTrue("sum must stay inside range", a + b <= 5);
                assertEquals(String.valueOf(a + b), q.options.get(q.correctIndex));
            }
        }
    }

    @Test
    public void subtractionNeverGoesBelowZero() {
        for (Skill skill : CurriculumGuide.all()) {
            if (!Topic.SUBTRACTION.equals(skill.topic)) continue;
            for (long seed = 1; seed <= 300; seed++) {
                for (Question q : new DrillGenerator(new Random(seed))
                        .generate(skill, skill.minNumber, skill.maxNumber, 10, fruits())) {
                    for (String o : q.options) assertTrue("negative option " + o, Integer.parseInt(o) >= 0);
                    String[] sides = q.visual.split("\\s\\u2212\\s");
                    Fruit f = null;
                    for (Fruit fr : fruits()) if (q.visual.contains(fr.emoji)) f = fr;
                    int a = count(sides[0], f.emoji), b = count(sides[1], f.emoji);
                    assertTrue(b <= a);
                    assertTrue(a <= skill.maxNumber);
                    assertEquals(String.valueOf(a - b), q.options.get(q.correctIndex));
                }
            }
        }
    }

    @Test
    public void countingVisualMatchesAnswerAndStaysInRange() {
        Skill skill = CurriculumGuide.byId("G1-CNT-1");
        for (long seed = 1; seed <= 100; seed++) {
            for (Question q : new DrillGenerator(new Random(seed)).generate(skill, 1, 20, 10, fruits())) {
                Fruit f = null;
                for (Fruit fr : fruits()) if (q.visual.contains(fr.emoji)) f = fr;
                int n = count(q.visual, f.emoji);
                assertTrue(n >= 1 && n <= 20);
                assertEquals(String.valueOf(n), q.options.get(q.correctIndex));
            }
        }
    }

    @Test
    public void numberRecognitionShowsNumeralAndAnswersWithWord() {
        Skill skill = CurriculumGuide.byId("G1-NUM-2");
        for (long seed = 1; seed <= 100; seed++) {
            List<Question> questions = new DrillGenerator(new Random(seed)).generate(skill, 1, 100, 30, fruits());
            int previous = Integer.MIN_VALUE;
            for (Question q : questions) {
                int n = Integer.parseInt(q.visual);
                assertTrue(n >= 1 && n <= 100);
                assertTrue(previous == Integer.MIN_VALUE || Math.abs((long) n - previous) > 1);
                assertEquals(NumberWords.of(n), q.options.get(q.correctIndex));
                previous = n;
            }
        }
    }

    @Test
    public void comparingPicksTheRightGroupOrNumber() {
        Skill groups = CurriculumGuide.byId("K-CMP-1");
        Skill numbers = CurriculumGuide.byId("G1-CMP-2");
        for (long seed = 1; seed <= 200; seed++) {
            for (Question q : new DrillGenerator(new Random(seed)).generate(groups, 1, 10, 6, fruits())) {
                assertTrue(q.optionsAreVisual);
                List<Integer> counts = new ArrayList<>();
                for (String o : q.options) {
                    int c = 0;
                    for (Fruit fr : fruits()) c += count(o, fr.emoji);
                    counts.add(c);
                }
                assertEquals(4, new HashSet<>(counts).size());     // four DIFFERENT group sizes
                boolean most = q.prompt.contains("MOST");
                int target = most ? java.util.Collections.max(counts) : java.util.Collections.min(counts);
                assertEquals(target, (int) counts.get(q.correctIndex));
            }
            for (Question q : new DrillGenerator(new Random(seed)).generate(numbers, 1, 100, 6, fruits())) {
                List<Integer> shown = numbersIn(q.visual);
                assertEquals(4, new HashSet<>(shown).size());
                boolean big = q.prompt.contains("BIGGEST");
                int target = big ? java.util.Collections.max(shown) : java.util.Collections.min(shown);
                assertEquals(String.valueOf(target), q.options.get(q.correctIndex));
            }
        }
    }

    @Test
    public void narrowedRangeIsRespected() {
        Skill skill = CurriculumGuide.byId("G1-ADD-2");              // normally up to 20
        for (long seed = 1; seed <= 100; seed++) {
            for (Question q : new DrillGenerator(new Random(seed)).generate(skill, 1, 6, 10, fruits())) {
                assertTrue(Integer.parseInt(q.options.get(q.correctIndex)) <= 6);
            }
        }
    }

    @Test
    public void validateRangeRejectsBadInput() {
        Skill add = CurriculumGuide.byId("K-ADD-1");
        assertNull(DrillGenerator.validateRange(add, 1, 5));
        assertNull(DrillGenerator.validateRange(add, 1, 10));
        assertNotNullMsg(DrillGenerator.validateRange(add, 1, 11));  // above the skill's range
        assertNotNullMsg(DrillGenerator.validateRange(add, 1, 1));   // too small for addition
        Skill cmp = CurriculumGuide.byId("K-CMP-1");
        assertNotNullMsg(DrillGenerator.validateRange(cmp, 1, 3));   // needs 4 different numbers
        assertNull(DrillGenerator.validateRange(cmp, 1, 4));
    }

    private static void assertNotNullMsg(String s) { assertNotNull(s); }

    @Test(expected = IllegalArgumentException.class)
    public void generateThrowsOnInvalidRange() {
        new DrillGenerator(new Random(1)).generate(CurriculumGuide.byId("K-ADD-1"), 1, 11, 5, fruits());
    }

    @Test
    public void worksWithTooFewApiFruits() {
        List<Fruit> one = new ArrayList<>();
        one.add(new Fruit("apple", "\uD83C\uDF4E"));
        List<Question> qs = new DrillGenerator(new Random(3)).generate(CurriculumGuide.byId("K-CMP-1"), 1, 10, 5, one);
        assertEquals(5, qs.size());
    }

    @Test
    public void pluralsAndQuantities() {
        assertEquals("3 apples", new Fruit("apple", "x").qty(3));
        assertEquals("1 apple", new Fruit("apple", "x").qty(1));
        assertEquals("2 strawberries", new Fruit("strawberry", "x").qty(2));
        assertEquals("2 cherries", new Fruit("cherry", "x").qty(2));
        assertEquals("2 peaches", new Fruit("peach", "x").qty(2));
        assertEquals("2 mangoes", new Fruit("mango", "x").qty(2));
        assertEquals("4 bananas", new Fruit("banana", "x").qty(4));
    }

    @Test
    public void numberWords() {
        assertEquals("seven", NumberWords.of(7));
        assertEquals("nineteen", NumberWords.of(19));
        assertEquals("twenty", NumberWords.of(20));
        assertEquals("forty-two", NumberWords.of(42));
        assertEquals("one hundred", NumberWords.of(100));
    }

    @Test
    public void gradeContentIsSeparate() {
        Set<String> kinder = new HashSet<>();
        for (Skill s : CurriculumGuide.forGrade("Kinder")) kinder.add(s.id);
        for (Skill s : CurriculumGuide.forGrade("1")) assertFalse(kinder.contains(s.id));
        assertEquals(5, CurriculumGuide.forGrade("Kinder").size());
        assertEquals(5, CurriculumGuide.forGrade("1").size());
        Set<String> kinderTopics = new HashSet<>();
        Set<String> gradeOneTopics = new HashSet<>();
        for (Skill s : CurriculumGuide.forGrade("Kinder")) assertTrue(kinderTopics.add(s.topic));
        for (Skill s : CurriculumGuide.forGrade("1")) assertTrue(gradeOneTopics.add(s.topic));
        for (Skill s : CurriculumGuide.forGrade("Kinder")) {
            assertTrue(s.maxNumber <= 10);
            assertNull(DrillGenerator.validateRange(s, s.minNumber, 10));
        }
    }
}
