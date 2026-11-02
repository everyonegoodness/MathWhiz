package com.mathwhiz.app.model;

/** A fruit fact ready to display. fromApi=false means the built-in fallback list was used. */
public final class FactResult {
    public final String fruitName;
    public final String emoji;
    public final String text;
    public final boolean fromApi;

    public FactResult(String fruitName, String emoji, String text, boolean fromApi) {
        this.fruitName = fruitName;
        this.emoji = emoji;
        this.text = text;
        this.fromApi = fromApi;
    }
}
