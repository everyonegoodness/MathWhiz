package com.mathwhiz.app.model;

import java.util.List;

/** Fruits available for questions and whether they came from Fruityvice or the built-in list. */
public final class FruitsResult {
    public final List<Fruit> fruits;
    public final boolean fromApi;

    public FruitsResult(List<Fruit> fruits, boolean fromApi) {
        this.fruits = fruits;
        this.fromApi = fromApi;
    }
}
