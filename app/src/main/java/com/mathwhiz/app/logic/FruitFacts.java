package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.FruitDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Turns raw Fruityvice data into one short, kid-friendly sentence. Returns null if there is nothing usable. */
public final class FruitFacts {
    private FruitFacts() {}

    public static String fromDto(FruitDto dto, Random rnd) {
        if (dto == null || dto.name == null || dto.name.trim().isEmpty()) return null;
        String name = dto.name.trim().toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();

        if (dto.family != null && !dto.family.trim().isEmpty()) {
            options.add("The " + name + " belongs to the " + dto.family.trim() + " family of plants.");
        }
        if (dto.nutritions != null) {
            if (dto.nutritions.sugar != null && dto.nutritions.sugar >= 0) {
                options.add("A 100-gram helping of " + name + " has about " + num(dto.nutritions.sugar)
                        + " grams of sugar.");
            }
            if (dto.nutritions.calories != null && dto.nutritions.calories >= 0) {
                options.add("A 100-gram helping of " + name + " gives your body about "
                        + num(dto.nutritions.calories) + " calories of energy.");
            }
        }
        if (options.isEmpty()) return null;
        return options.get(rnd.nextInt(options.size()));
    }

    static String num(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.format(Locale.US, "%.1f", v);
    }
}
