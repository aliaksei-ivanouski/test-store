package org.aivanouski.store.ingredient;

import org.aivanouski.store.error.IllegalIngredientsFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IngredientValidatorTest {

    @Test
    void validateIngredientsTest() {
        // given
        UUID uuid = UUID.randomUUID();
        List<String> requestedIngredients = Collections.singletonList(uuid.toString());
        List<Ingredient> allowedIngredients = Collections.singletonList(new Ingredient.Builder().setId(uuid).build());

        // when
        IngredientValidator.validateIngredients(requestedIngredients, allowedIngredients);

        // then
        // no exception should be thrown
    }

    @Test
    void validateDeliveryAddressTest_notAllowedExists() {
        // given
        UUID notAllowed = UUID.randomUUID();
        List<String> requestedIngredients = Collections.singletonList(notAllowed.toString());
        List<Ingredient> allowedIngredients = Collections.singletonList(new Ingredient.Builder().setId(UUID.randomUUID()).build());

        // when
        IllegalIngredientsFoundException exception = assertThrows(
                IllegalIngredientsFoundException.class,
                () -> IngredientValidator.validateIngredients(requestedIngredients, allowedIngredients)
        );

        // then
        assertEquals("Illegal ingredients found: " + notAllowed, exception.getMessage());
    }
}
