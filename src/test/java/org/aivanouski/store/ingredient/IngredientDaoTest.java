package org.aivanouski.store.ingredient;

import org.aivanouski.store.Fixtures;
import org.aivanouski.store.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientDaoTest extends TestBase {

    private final IngredientCacheDAO cache = IngredientCacheDAO.getInstance();
    private final IngredientDAO orderDao = IngredientDAOImpl.getInstance();

    @BeforeEach
    public void setup() {
        Fixtures.createIngredients();
    }

    @Test
    void getAllIngredientsTest() {
        // given

        // when
        List<Ingredient> cacheAllIngredients = cache.getAllIngredients();

        // then
        assertTrue(cacheAllIngredients.isEmpty());

        // when
        List<Ingredient> ingredients = orderDao.getAllIngredients();
        cacheAllIngredients = cache.getAllIngredients();

        // then
        assertNotNull(ingredients);
        assertNotNull(cacheAllIngredients);
        assertEquals(4, ingredients.size());
        assertEquals(4, cacheAllIngredients.size());
        assertTrue(ingredients.containsAll(cacheAllIngredients));
    }
}
