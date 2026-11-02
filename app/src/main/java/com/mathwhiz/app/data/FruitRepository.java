package com.mathwhiz.app.data;

import com.mathwhiz.app.logic.FruitCatalog;
import com.mathwhiz.app.logic.FruitFacts;
import com.mathwhiz.app.model.FactResult;
import com.mathwhiz.app.model.Fruit;
import com.mathwhiz.app.model.FruitDto;
import com.mathwhiz.app.model.FruitsResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fruityvice integration with a timeout and a guaranteed fallback: these methods ALWAYS deliver a result
 * (onError is never called), so class mode never breaks because the API is slow, down, or returns odd data.
 */
public final class FruitRepository {
    private static final FruitRepository INSTANCE = new FruitRepository();

    public static FruitRepository get() { return INSTANCE; }

    private final FruityviceApi api;
    private final Random rnd = new Random();
    private FruitsResult cached;                                   // API list is fetched once per app run
    private final Map<String, String> apiNames = new HashMap<>();  // "grape" -> "Grapes" (name the API uses)

    private FruitRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
        api = new Retrofit.Builder()
                .baseUrl("https://www.fruityvice.com/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FruityviceApi.class);
    }

    /** Fruits for the questions: from the API when possible (only those the app has a picture for). */
    public void loadFruits(final DataCallback<FruitsResult> cb) {
        if (cached != null) {
            cb.onSuccess(cached);
            return;
        }
        api.getAll().enqueue(new Callback<List<FruitDto>>() {
            @Override
            public void onResponse(Call<List<FruitDto>> call, Response<List<FruitDto>> response) {
                List<FruitDto> body = response.body();
                if (response.isSuccessful() && body != null) {
                    Map<String, Fruit> found = new LinkedHashMap<>();
                    for (FruitDto dto : body) {
                        if (dto == null || dto.name == null) continue;
                        String norm = FruitCatalog.normalize(dto.name);
                        String emoji = FruitCatalog.emojiFor(norm);
                        if (emoji == null || found.containsKey(norm)) continue;   // skip fruits with no picture
                        found.put(norm, new Fruit(norm, emoji));
                        apiNames.put(norm, dto.name);
                    }
                    if (found.size() >= 4) {
                        cached = new FruitsResult(new ArrayList<>(found.values()), true);
                        cb.onSuccess(cached);
                        return;
                    }
                }
                cb.onSuccess(new FruitsResult(FruitCatalog.fallbackFruits(), false));
            }

            @Override
            public void onFailure(Call<List<FruitDto>> call, Throwable t) {
                cb.onSuccess(new FruitsResult(FruitCatalog.fallbackFruits(), false));
            }
        });
    }

    /** A kid-friendly fact about one fruit; falls back to the offline list on any problem. */
    public void loadFact(final String fruitName, final DataCallback<FactResult> cb) {
        final String norm = FruitCatalog.normalize(fruitName);
        if (norm.isEmpty()) {
            cb.onSuccess(FruitCatalog.randomFallbackFact(rnd));
            return;
        }
        String apiName = apiNames.containsKey(norm) ? apiNames.get(norm) : norm;
        api.getFruit(apiName).enqueue(new Callback<FruitDto>() {
            @Override
            public void onResponse(Call<FruitDto> call, Response<FruitDto> response) {
                String text = response.isSuccessful() ? FruitFacts.fromDto(response.body(), rnd) : null;
                if (text != null) {
                    String emoji = FruitCatalog.emojiFor(norm);
                    cb.onSuccess(new FactResult(norm, emoji == null ? "\uD83C\uDF4E" : emoji, text, true));
                } else {
                    cb.onSuccess(FruitCatalog.fallbackFact(norm, rnd));
                }
            }

            @Override
            public void onFailure(Call<FruitDto> call, Throwable t) {
                cb.onSuccess(FruitCatalog.fallbackFact(norm, rnd));
            }
        });
    }
}
