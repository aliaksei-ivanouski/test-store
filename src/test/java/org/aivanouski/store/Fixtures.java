package org.aivanouski.store;

import org.aivanouski.store.config.PostgresConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Fixtures {

    private Fixtures() {
    }

    public static void createOrders() {
        String sql = "drop table if exists \"t_orders\";\n" +
                "create table \"t_orders\"\n" +
                "(\n" +
                "    \"id\"          uuid primary key default gen_random_uuid(),\n" +
                "    \"building\"    int              not null,\n" +
                "    \"room\"        int              not null,\n" +
                "    \"status\"      text             not null,\n" +
                "    \"ingredients\" uuid[]           default array[]::uuid[],\n" +
                "    \"created_at\"  timestamp        default now(),\n" +
                "    \"updated_at\"  timestamp        default now()\n" +
                ");";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
        }
    }

    public static void deleteOrders() {
        String sql = "delete from \"t_orders\";";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
        }
    }

    public static void createIngredients() {
        String sql = "drop table if exists \"t_ingredients\";\n" +
                "create table \"t_ingredients\"\n" +
                "(\n" +
                "    \"id\"         uuid primary key default gen_random_uuid(),\n" +
                "    \"name\"       text             not null,\n" +
                "    \"created_at\" timestamp        default now(),\n" +
                "    \"updated_at\" timestamp        default now()\n" +
                ");\n" +
                "\n" +
                "insert into \"t_ingredients\" (\"name\") values ('dark chocolate');\n" +
                "insert into \"t_ingredients\" (\"name\") values ('milk chocolate');\n" +
                "insert into \"t_ingredients\" (\"name\") values ('whipped cream');\n" +
                "insert into \"t_ingredients\" (\"name\") values ('hazelnuts');";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
        }
    }

    public static void deleteIngredients() {
        String sql = "delete from \"t_ingredients\";";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
        }
    }
}
