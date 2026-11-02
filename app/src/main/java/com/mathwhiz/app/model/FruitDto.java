package com.mathwhiz.app.model;

/** Shape of one Fruityvice fruit (GET /api/fruit/{name} and items of /api/fruit/all). Missing fields stay null. */
public class FruitDto {
    public String name;
    public String family;
    public String genus;
    public String order;
    public Nutritions nutritions;

    public static class Nutritions {
        public Double calories;
        public Double fat;
        public Double sugar;
        public Double carbohydrates;
        public Double protein;
    }
}
