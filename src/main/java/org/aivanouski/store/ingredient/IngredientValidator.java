package org.aivanouski.store.ingredient;

import org.aivanouski.store.error.IllegalIngredientsFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class IngredientValidator {

    private IngredientValidator() {
    }

    public static void validateIngredients(List<String> requestedIngredients, List<Ingredient> allowedIngredients) {
        List<UUID> requestedIds = requestedIngredients.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        List<UUID> allowedIds = allowedIngredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
        final List<UUID> notAllowed = requestedIds.stream()
                .filter(id -> !allowedIds.contains(id))
                .collect(Collectors.toList());

        if (!notAllowed.isEmpty()) {
            throw new IllegalIngredientsFoundException("Illegal ingredients found: "
                    + notAllowed.stream().map(UUID::toString).collect(Collectors.joining(", ")));
        }
    }
}
