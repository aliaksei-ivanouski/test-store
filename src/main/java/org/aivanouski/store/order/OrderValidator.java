package org.aivanouski.store.order;

import org.aivanouski.store.config.PropertiesConfig;
import org.aivanouski.store.error.IllegalOrderStateException;
import org.aivanouski.store.error.IllegalStatusException;
import org.aivanouski.store.error.InvalidAddressException;

import java.util.Properties;

import static org.aivanouski.store.order.OrderStatus.COMPLETED;

public class OrderValidator {

    private static final Properties properties = PropertiesConfig.PROPERTIES;

    private OrderValidator() {
    }

    public static void validateDeliveryAddress(int building, int room) {
        int maxBuildingNumber = Integer.parseInt(properties.getProperty("app.max-building-number"));
        int maxRoomNumber = Integer.parseInt(properties.getProperty("app.max-room-number"));
        if (building > maxBuildingNumber || room > maxRoomNumber) {
            throw new InvalidAddressException(String.format("Building and room must not exceed the following values: " +
                    "building: %d and room %d", maxBuildingNumber, maxRoomNumber));
        }
    }

    public static void validateOrderBeforeDelivery(Order order) {
        if (COMPLETED == order.getStatus()) {
            throw new IllegalStatusException("Order already completed, please wait for deliver or cancel it.");
        }
        if (order.getIngredients().isEmpty()) {
            throw new IllegalOrderStateException("Order must have at least one ingredient to be passed to chef.");
        }
    }
}
