package org.aivanouski.store.ingredient;

import org.aivanouski.store.config.PostgresConfig;
import org.aivanouski.store.error.DatabaseOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngredientDAOImpl implements IngredientDAO {

    private static final Logger log = LoggerFactory.getLogger(IngredientDAOImpl.class);

    private final IngredientCacheDAO cache = IngredientCacheDAO.getInstance();

    private static class IngredientDAOImplHelper {
        private static final IngredientDAOImpl INSTANCE = new IngredientDAOImpl();
    }

    public static IngredientDAOImpl getInstance() {
        return IngredientDAOImplHelper.INSTANCE;
    }

    private IngredientDAOImpl() {}

    @Override
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM t_ingredients";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeQuery();

            cache.deleteAllIngredients();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                ingredients.add(ingredient);
                //put in cache
                cache.createIngredient(ingredient);
            }
        } catch (SQLException e) {
            String message = "Failed to get all ingredients";
            log.error(message, e);
            throw new DatabaseOperationException(message);
        }
        return ingredients;
    }
}
