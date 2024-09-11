package org.aivanouski.store.order;

import java.util.List;
import java.util.UUID;

public interface OrderDAO {

    Order createOrder(Order order);
    Order getOrderById(UUID id);
    List<Order> getOrdersByStatus(OrderStatus status);
    void addIngredients(UUID orderId, List<UUID> ingredientIds);
    Order updateOrder(Order order);
    void deleteOrder(UUID id);
}
