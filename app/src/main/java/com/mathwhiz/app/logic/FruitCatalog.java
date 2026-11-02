package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.FactResult;
import com.mathwhiz.app.model.Fruit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Built-in fruits (with their emoji "pictures") and the local fallback facts used when Fruityvice is
 * unavailable. To use real illustrations later, replace the emoji with drawable resources.
 */
public final class FruitCatalog {
    private static final Map<String, String> EMOJI = new LinkedHashMap<>();
    private static final Map<String, String> FACTS = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();
    private static final String[] GENERIC_FACTS = {
            "Fruits are packed with vitamins that help your body grow strong.",
            "Eating fruits of many different colors is good for your body.",
            "Most fruits grow from flowers on plants and trees."
    };

    static {
        EMOJI.put("apple", "\uD83C\uDF4E");
        EMOJI.put("banana", "\uD83C\uDF4C");
        EMOJI.put("orange", "\uD83C\uDF4A");
        EMOJI.put("grape", "\uD83C\uDF47");
        EMOJI.put("strawberry", "\uD83C\uDF53");
        EMOJI.put("watermelon", "\uD83C\uDF49");
        EMOJI.put("pineapple", "\uD83C\uDF4D");
        EMOJI.put("pear", "\uD83C\uDF50");
        EMOJI.put("cherry", "\uD83C\uDF52");
        EMOJI.put("lemon", "\uD83C\uDF4B");
        EMOJI.put("peach", "\uD83C\uDF51");
        EMOJI.put("melon", "\uD83C\uDF48");
        EMOJI.put("kiwi", "\uD83E\uDD5D");
        EMOJI.put("mango", "\uD83E\uDD6D");
        EMOJI.put("tomato", "\uD83C\uDF45");
        EMOJI.put("coconut", "\uD83E\uDD65");
        EMOJI.put("avocado", "\uD83E\uDD51");

        ALIASES.put("grapes", "grape");
        ALIASES.put("kiwifruit", "kiwi");
        ALIASES.put("kiwi fruit", "kiwi");
        ALIASES.put("cherries", "cherry");
        ALIASES.put("strawberries", "strawberry");

        FACTS.put("apple", "Apples float in water because part of an apple is air.");
        FACTS.put("banana", "Bananas grow in bunches called hands.");
        FACTS.put("orange", "Oranges have vitamin C, which helps your body stay healthy.");
        FACTS.put("grape", "Grapes grow in bunches on vines.");
        FACTS.put("strawberry", "A strawberry has its tiny seeds on the outside.");
        FACTS.put("watermelon", "Watermelon is mostly made of water.");
        FACTS.put("pineapple", "A pineapple grows on a plant close to the ground, not on a tree.");
        FACTS.put("pear", "Pears are sweet and juicy and shaped a bit like a bell.");
        FACTS.put("cherry", "Cherries grow on trees and each one has a hard pit inside.");
        FACTS.put("lemon", "Lemons taste sour, and they grow on trees.");
        FACTS.put("peach", "A peach has fuzzy skin and one big seed in the middle.");
        FACTS.put("melon", "Melons grow on vines along the ground.");
        FACTS.put("kiwi", "A kiwi is fuzzy and brown outside but green inside.");
        FACTS.put("mango", "Mangoes grow on tall trees in warm places.");
        FACTS.put("tomato", "A tomato is really a fruit, even though many people cook it like a vegetable.");
        FACTS.put("coconut", "Coconuts grow on tall palm trees.");
        FACTS.put("avocado", "An avocado has one big seed in the middle.");
    }

    private FruitCatalog() {}

    /** Lowercases and maps plural/alias API names to the catalog name (e.g. "Grapes" -> "grape"). */
    public static String normalize(String name) {
        if (name == null) return "";
        String n = name.trim().toLowerCase(Locale.ROOT);
        String alias = ALIASES.get(n);
        return alias != null ? alias : n;
    }

    /** Emoji picture for a fruit, or null if the app has no picture for it. */
    public static String emojiFor(String name) {
        return EMOJI.get(normalize(name));
    }

    public static List<Fruit> fallbackFruits() {
        List<Fruit> out = new ArrayList<>();
        for (Map.Entry<String, String> e : EMOJI.entrySet()) out.add(new Fruit(e.getKey(), e.getValue()));
        return out;
    }

    /** Number of facts in the offline list (17 fruit-specific + 3 general). */
    public static int fallbackFactCount() {
        return FACTS.size() + GENERIC_FACTS.length;
    }

    /** Offline fact for a specific fruit; a general fruit fact if the fruit has none. */
    public static FactResult fallbackFact(String fruitName, Random rnd) {
        String n = normalize(fruitName);
        String emoji = EMOJI.get(n);
        String text = FACTS.get(n);
        if (text == null) {
            text = GENERIC_FACTS[rnd.nextInt(GENERIC_FACTS.length)];
        }
        return new FactResult(n.isEmpty() ? "fruit" : n, emoji == null ? "\uD83C\uDF4E" : emoji, text, false);
    }

    /** Offline fact for a random fruit. */
    public static FactResult randomFallbackFact(Random rnd) {
        List<Fruit> fruits = fallbackFruits();
        return fallbackFact(fruits.get(rnd.nextInt(fruits.size())).name, rnd);
    }
}
