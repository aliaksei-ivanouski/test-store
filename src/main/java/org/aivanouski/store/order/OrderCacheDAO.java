package org.aivanouski.store.order;

import com.google.gson.Gson;
import org.aivanouski.store.config.GsonConfig;
import org.aivanouski.store.config.RedisConfig;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderCacheDAO implements OrderDAO {

    private static final JedisPooled cache = RedisConfig.getInstance().getJedis();
    private static final Gson gson = GsonConfig.getInstance().getGson();
    private static final String KEY = "order";

    private static class OrderCacheDAOHelper {
        private static final OrderCacheDAO INSTANCE = new OrderCacheDAO();
    }

    public static OrderCacheDAO getInstance() {
        return OrderCacheDAOHelper.INSTANCE;
    }

    private OrderCacheDAO() {
    }

    @Override
    public Order createOrder(Order order) {
        cache.hset(KEY, order.getId().toString(), gson.toJson(order));
        return order;
    }

    @Override
    public Order getOrderById(UUID id) {
        String json = cache.hget(KEY, id.toString());
        return json != null
                ? gson.fromJson(json, Order.class)
                : null;
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        Map<String, String> map = cache.hgetAll(KEY);
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        } else {
            return map.values().stream()
                    .map(json -> gson.fromJson(json, Order.class))
                    .filter(order -> order.getStatus().equals(status))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @Override
    public void addIngredients(UUID orderId, List<UUID> ingredientIds) {
        Order order = getOrderById(orderId);
        order.getIngredients().addAll(ingredientIds);
        updateOrder(order);
    }

    @Override
    public Order updateOrder(Order order) {
        cache.hdel(KEY, order.getId().toString());
        cache.hset(KEY, order.getId().toString(), gson.toJson(order));
        return order;
    }

    @Override
    public void deleteOrder(UUID id) {
        cache.hdel(KEY, id.toString());
    }
}
