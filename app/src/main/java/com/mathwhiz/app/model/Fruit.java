package com.mathwhiz.app.model;

/** A fruit used in questions: lowercase singular name plus the emoji used as its picture. */
public final class Fruit {
    public final String name;
    public final String emoji;

    public Fruit(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String plural() {
        String n = name;
        if (n.length() > 1 && n.endsWith("y") && "aeiou".indexOf(n.charAt(n.length() - 2)) < 0) {
            return n.substring(0, n.length() - 1) + "ies";
        }
        if (n.endsWith("ch") || n.endsWith("sh") || n.endsWith("s") || n.endsWith("x")) return n + "es";
        if (n.equals("mango") || n.equals("tomato")) return n + "es";
        return n + "s";
    }

    /** "1 apple", "3 apples". */
    public String qty(int count) {
        return count + " " + (count == 1 ? name : plural());
    }
}
