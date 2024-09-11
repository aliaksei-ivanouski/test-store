package org.aivanouski.store.ingredient;

import com.google.gson.Gson;
import org.aivanouski.store.config.GsonConfig;
import org.aivanouski.store.config.RedisConfig;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientCacheDAO implements IngredientDAO {

    private static final JedisPooled cache = RedisConfig.getInstance().getJedis();
    private static final Gson gson = GsonConfig.getInstance().getGson();
    private static final String KEY = "ingredient";

    private static class IngredientCacheDAOHelper {
        private static final IngredientCacheDAO INSTANCE = new IngredientCacheDAO();
    }

    public static IngredientCacheDAO getInstance() {
        return IngredientCacheDAOHelper.INSTANCE;
    }

    private IngredientCacheDAO() {}

    public void createIngredient(Ingredient ingredient) {
        cache.hset(KEY, ingredient.getId().toString(), gson.toJson(ingredient));
    }

    @Override
    public List<Ingredient> getAllIngredients() {
        return cache.hgetAll(KEY).values().stream()
                .map(json -> gson.fromJson(json, Ingredient.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void deleteAllIngredients() {
        cache.del(KEY);
    }
}
