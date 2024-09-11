package org.aivanouski.store.order;

import org.aivanouski.store.Fixtures;
import org.aivanouski.store.TestBase;
import org.aivanouski.store.error.InvalidAddressException;
import org.aivanouski.store.error.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.aivanouski.store.order.OrderStatus.COMPLETED;
import static org.aivanouski.store.order.OrderStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderDaoTest extends TestBase {

    private final OrderDAO cache = OrderCacheDAO.getInstance();
    private final OrderDAO orderDao = OrderDAOImpl.getInstance();

    private UUID orderId;

    @BeforeEach
    public void setup() {
        Fixtures.createOrders();
    }

    @Test
    void createOrderTest() {
        // given
        Order order = orderDao.createOrder(new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .build());

        // when
        this.orderId = order.getId();
        Order cacheResult = cache.getOrderById(orderId);
        Order dbResult = orderDao.getOrderById(orderId);

        // then
        assertNotNull(dbResult);
        assertEquals(cacheResult, dbResult);
        assertEquals(order.getId(), dbResult.getId());
        assertEquals(5, dbResult.getBuilding());
        assertEquals(32, dbResult.getRoom());
        assertEquals(IN_PROGRESS, dbResult.getStatus());
    }

    @Test
    void createOrderTest_invalidBuilding() {
        // given

        // when
        InvalidAddressException exception = assertThrows(
                InvalidAddressException.class,
                () -> new Order.Builder()
                        .setBuilding(200)
                        .setRoom(32)
                        .setStatus(IN_PROGRESS)
                        .build()
        );

        // then
        assertEquals("Building and room must not exceed the following values: building: 50 and room 120", exception.getMessage());
    }

    @Test
    void createOrderTest_invalidRoom() {
        // given

        // when
        InvalidAddressException exception = assertThrows(
                InvalidAddressException.class,
                () -> new Order.Builder()
                        .setBuilding(15)
                        .setRoom(130)
                        .setStatus(IN_PROGRESS)
                        .build()
        );

        // then
        assertEquals("Building and room must not exceed the following values: building: 50 and room 120", exception.getMessage());
    }

    @Test
    void getOrderByIdTest() {
        // given
        Order order = orderDao.createOrder(new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .build());

        this.orderId = order.getId();
        Order cacheResult = cache.getOrderById(orderId);
        assertNotNull(cacheResult);

        cache.deleteOrder(order.getId());
        cacheResult = cache.getOrderById(orderId);
        assertNull(cacheResult);

        // when
        Order result = orderDao.getOrderById(orderId);

        // then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(5, result.getBuilding());
        assertEquals(32, result.getRoom());
        assertEquals(IN_PROGRESS, result.getStatus());
    }

    @Test
    void getOrderByIdTest_orderNotFound() {
        // given
        UUID id = UUID.randomUUID();

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderDao.getOrderById(id)
        );

        // then
        assertEquals(String.format("Order not found, id: %s", id), exception.getMessage());
    }

    @Test
    void getOrderByStatusTest() {
        // given
        Order order = orderDao.createOrder(new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .build());

        List<Order> cacheResult = cache.getOrdersByStatus(order.getStatus());
        assertNotNull(cacheResult);
        assertEquals(1, cacheResult.size());

        cache.deleteOrder(order.getId());
        cacheResult = cache.getOrdersByStatus(order.getStatus());
        assertTrue(cacheResult.isEmpty());

        // when
        List<Order> result = orderDao.getOrdersByStatus(order.getStatus());

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
        assertEquals(5, result.get(0).getBuilding());
        assertEquals(32, result.get(0).getRoom());
        assertEquals(IN_PROGRESS, result.get(0).getStatus());
    }

    @Test
    void updateOrderTest() {
        // given
        Order.Builder orderBulder = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS);
        Order order = orderDao.createOrder(orderBulder.build());

        // when
        this.orderId = order.getId();
        Order cacheResult = cache.getOrderById(orderId);
        cache.deleteOrder(order.getId());
        Order dbResult = orderDao.getOrderById(orderId);

        // then
        assertNotNull(dbResult);
        assertEquals(cacheResult, dbResult);
        assertEquals(order.getId(), dbResult.getId());
        assertEquals(5, dbResult.getBuilding());
        assertEquals(32, dbResult.getRoom());
        assertEquals(IN_PROGRESS, dbResult.getStatus());

        // given
        orderBulder
                .setId(dbResult.getId())
                .setBuilding(10)
                .setRoom(11)
                .setStatus(COMPLETED);
        orderDao.updateOrder(orderBulder.build());

        // when
        cacheResult = cache.getOrderById(orderId);
        dbResult = orderDao.getOrderById(orderId);

        // then
        assertNotNull(dbResult);
        assertEquals(cacheResult, dbResult);
        assertEquals(order.getId(), dbResult.getId());
        assertEquals(10, dbResult.getBuilding());
        assertEquals(11, dbResult.getRoom());
        assertEquals(COMPLETED, dbResult.getStatus());
    }

    @Test
    void updateOrderTest_updateCache() {
        // given
        Order.Builder orderBulder = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS);
        Order order = orderDao.createOrder(orderBulder.build());

        // when
        this.orderId = order.getId();

        order.setBuilding(12);
        order.setRoom(20);
        order.setStatus(COMPLETED);
        Order cacheResult = cache.updateOrder(order);
        cache.deleteOrder(order.getId());

        // then
        assertNotNull(cacheResult);
        assertEquals(orderId, cacheResult.getId());
        assertEquals(12, cacheResult.getBuilding());
        assertEquals(20, cacheResult.getRoom());
        assertEquals(COMPLETED, cacheResult.getStatus());
    }

    @Test
    void addIngredientsTest() {
        // given
        Order.Builder orderBulder = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS);
        Order order = orderDao.createOrder(orderBulder.build());
        List<UUID> ingredients = Collections.singletonList(UUID.randomUUID());
        order.setIngredients(ingredients);

        // when
        orderDao.addIngredients(order.getId(), order.getIngredients());

        // then
        this.orderId = order.getId();
        Order cacheResult = cache.getOrderById(orderId);
        cache.deleteOrder(order.getId());
        Order dbResult = orderDao.getOrderById(orderId);
        assertNotNull(cacheResult);
        assertNotNull(dbResult);
        assertEquals(cacheResult, dbResult);
        assertEquals(ingredients.size(), cacheResult.getIngredients().size());
        assertTrue(ingredients.containsAll(cacheResult.getIngredients()));
        assertEquals(ingredients.size(), dbResult.getIngredients().size());
        assertTrue(ingredients.containsAll(dbResult.getIngredients()));
    }

    @Test
    void addIngredientsTest_addToCache() {
        // given
        Order.Builder orderBulder = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS);
        Order order = orderDao.createOrder(orderBulder.build());
        List<UUID> ingredients = Collections.singletonList(UUID.randomUUID());
        order.setIngredients(ingredients);

        // when
        cache.addIngredients(order.getId(), order.getIngredients());

        // then
        this.orderId = order.getId();
        Order cacheResult = cache.getOrderById(orderId);
        cache.deleteOrder(order.getId());
        assertNotNull(cacheResult);
        assertEquals(ingredients.size(), cacheResult.getIngredients().size());
        assertTrue(ingredients.containsAll(cacheResult.getIngredients()));
    }

    @Test
    void deleteOrderTest() {
        // given
        Order order = orderDao.createOrder(new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .build());

        // when
        this.orderId = order.getId();
        orderDao.deleteOrder(orderId);

        Order cacheResult = cache.getOrderById(orderId);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderDao.getOrderById(orderId)
        );

        // then
        assertNull(cacheResult);
        assertEquals(String.format("Order not found, id: %s", orderId), exception.getMessage());
    }

    @AfterEach
    void tearDown() {
        Fixtures.deleteOrders();
        if (orderId != null) {
            cache.deleteOrder(orderId);
        }
    }
}