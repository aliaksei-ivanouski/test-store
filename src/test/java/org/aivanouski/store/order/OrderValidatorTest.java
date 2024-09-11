package org.aivanouski.store.order;

import org.aivanouski.store.config.PropertiesConfig;
import org.aivanouski.store.error.IllegalOrderStateException;
import org.aivanouski.store.error.IllegalStatusException;
import org.aivanouski.store.error.InvalidAddressException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.aivanouski.store.order.OrderStatus.COMPLETED;
import static org.aivanouski.store.order.OrderStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderValidatorTest {

    @BeforeAll
    static void setUp() {
        PropertiesConfig.getInstance().init();
    }

    @Test
    void validateDeliveryAddressTest() {
        // given
        // when
        OrderValidator.validateDeliveryAddress(50, 120);

        // then
        // no exception should be thrown
    }

    @Test
    void validateDeliveryAddressTest_invalidBuilding() {
        // given
        // when
        InvalidAddressException exception = assertThrows(
                InvalidAddressException.class,
                () -> OrderValidator.validateDeliveryAddress(51, 120)
        );

        // then
        assertEquals("Building and room must not exceed the following values: building: 50 and room 120",
                exception.getMessage());
    }

    @Test
    void validateDeliveryAddressTest_invalidRoom() {
        // given
        // when
        InvalidAddressException exception = assertThrows(
                InvalidAddressException.class,
                () -> OrderValidator.validateDeliveryAddress(40, 121)
        );

        // then
        assertEquals("Building and room must not exceed the following values: building: 50 and room 120",
                exception.getMessage());
    }

    @Test
    void validateOrderBeforeDeliveryTest() {
        // given
        Order order = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .setIngredients(Collections.singletonList(UUID.randomUUID()))
                .build();

        // when
        OrderValidator.validateOrderBeforeDelivery(order);

        // then
        // no exception should be thrown
    }

    @Test
    void validateOrderBeforeDeliveryTest_orderAlreadyCompleted() {
        // given
        Order order = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(COMPLETED)
                .setIngredients(Collections.singletonList(UUID.randomUUID()))
                .build();

        // when
        IllegalStatusException exception = assertThrows(
                IllegalStatusException.class,
                () -> OrderValidator.validateOrderBeforeDelivery(order)
        );

        // then
        assertEquals("Order already completed, please wait for deliver or cancel it.",
                exception.getMessage());
    }

    @Test
    void validateOrderBeforeDeliveryTest_() {
        // given
        Order order = new Order.Builder()
                .setBuilding(5)
                .setRoom(32)
                .setStatus(IN_PROGRESS)
                .build();

        // when
        IllegalOrderStateException exception = assertThrows(
                IllegalOrderStateException.class,
                () -> OrderValidator.validateOrderBeforeDelivery(order)
        );

        // then
        assertEquals("Order must have at least one ingredient to be passed to chef.",
                exception.getMessage());
    }
}
