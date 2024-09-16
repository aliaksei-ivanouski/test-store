package org.aivanouski.store.order;

import org.aivanouski.store.config.PostgresConfig;
import org.aivanouski.store.error.DatabaseOperationException;
import org.aivanouski.store.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class OrderDAOImpl implements OrderDAO {

    private static final Logger log = LoggerFactory.getLogger(OrderDAOImpl.class);

    private final OrderDAO cache = OrderCacheDAO.getInstance();

    private static class OrderDAOImplHelper {
        private static final OrderDAOImpl INSTANCE = new OrderDAOImpl();
    }

    public static OrderDAOImpl getInstance() {
        return OrderDAOImplHelper.INSTANCE;
    }

    private OrderDAOImpl() {}

    @Override
    public Order createOrder(Order order) {
        String sql = "INSERT INTO t_orders (building, room, status, ingredients) VALUES (?, ?, ?, ?::uuid[])";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, order.getBuilding());
            statement.setInt(2, order.getRoom());
            statement.setString(3, order.getStatus().name());
            statement.setArray(4, connection.createArrayOf("uuid", order.getIngredients().toArray(new UUID[0])));
            statement.execute();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                //fetch to put in cache
                UUID id = UUID.fromString(rs.getString(1));
                Order orderById = this.getOrderById(id);
                cache.createOrder(orderById);
                return orderById;
            }
        } catch (SQLException e) {
            String message = String.format("Failed to create order, building: %d, room: %d", order.getBuilding(), order.getRoom());
            log.error(message, e);
            throw new DatabaseOperationException(message);
        }
        return null;
    }

    @Override
    public Order getOrderById(UUID id) {
        Order order = cache.getOrderById(id);
        if (order == null) {
            String sql = "SELECT id, building, room, status, ingredients::uuid[], created_at, updated_at FROM t_orders WHERE id = ?";
            try (Connection connection = PostgresConfig.getInstance().getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, id);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return new Order(
                            UUID.fromString(rs.getString(1)),
                            rs.getInt(2),
                            rs.getInt(3),
                            OrderStatus.valueOf(rs.getString(4)),
                            Arrays.asList((UUID[]) rs.getArray(5).getArray()),
                            rs.getTimestamp(6).toLocalDateTime(),
                            rs.getTimestamp(7).toLocalDateTime()
                    );
                }
            } catch (SQLException e) {
                String message = String.format("Failed to get order, id: %s", id);
                log.error(message, e);
                throw new DatabaseOperationException(message);
            }
        }
        if (order == null) {
            throw new NotFoundException(String.format("Order not found, id: %s", id));
        }
        return order;
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = cache.getOrdersByStatus(status);
        String sql = "SELECT id, building, room, status, ingredients::uuid[], created_at, updated_at FROM t_orders WHERE status = ?";
        if (orders.isEmpty()) {
            try (Connection connection = PostgresConfig.getInstance().getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, status.name());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    Order order = new Order(
                            UUID.fromString(rs.getString(1)),
                            rs.getInt(2),
                            rs.getInt(3),
                            OrderStatus.valueOf(rs.getString(4)),
                            Arrays.asList((UUID[]) rs.getArray(5).getArray()),
                            rs.getTimestamp(6).toLocalDateTime(),
                            rs.getTimestamp(7).toLocalDateTime()
                    );
                    orders.add(order);
                }
            } catch (SQLException e) {
                String message = String.format("Failed to get orders, status: %s", status);
                log.error(message, e);
                throw new DatabaseOperationException(message);
            }
        }
        return orders;
    }

    @Override
    public void addIngredients(UUID orderId, List<UUID> ingredientIds) {
        String sql = "UPDATE t_orders SET ingredients = ?::uuid[], updated_at = ? WHERE id = ?";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setArray(1, connection.createArrayOf("uuid", ingredientIds.toArray(new UUID[0])));
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setObject(3, orderId);
            statement.executeUpdate();

            cache.deleteOrder(orderId);
            Order orderById = this.getOrderById(orderId);
            cache.createOrder(orderById);
        } catch (SQLException e) {
            String message = String.format("Failed to add ingredients to order, id: %s", orderId);
            log.error(message, e);
            throw new DatabaseOperationException(message);
        }
    }

    @Override
    public Order updateOrder(Order order) {
        String sql = "UPDATE t_orders SET building = ?, room = ?, status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.getBuilding());
            statement.setInt(2, order.getRoom());
            statement.setString(3, order.getStatus().name());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            statement.setObject(5, order.getId());
            statement.executeUpdate();

            //put in cache
            cache.deleteOrder(order.getId());
            Order orderById = this.getOrderById(order.getId());
            cache.createOrder(orderById);
            return orderById;
        } catch (SQLException e) {
            String message = String.format("Failed to update order, id: %s", order.getId());
            log.error(message, e);
            throw new DatabaseOperationException(message);
        }
    }

    @Override
    public void deleteOrder(UUID id) {
        String sql = "DELETE FROM t_orders WHERE id = ?";
        try (Connection connection = PostgresConfig.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();

            cache.deleteOrder(id);
        } catch (SQLException e) {
            String message = String.format("Failed to delete order, id: %s", id);
            log.error(message, e);
            throw new DatabaseOperationException(message);
        }
    }
}
