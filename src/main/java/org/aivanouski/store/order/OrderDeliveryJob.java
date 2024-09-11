package org.aivanouski.store.order;

import org.aivanouski.store.config.PropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import static org.aivanouski.store.order.OrderStatus.COMPLETED;

public class OrderDeliveryJob {

    private static final Logger log = LoggerFactory.getLogger(OrderDeliveryJob.class);

    private static final Properties properties = PropertiesConfig.PROPERTIES;

    private final OrderDAO orderDAO = OrderDAOImpl.getInstance();

    private static class OrderDeliveryJobHelper {
        private static final OrderDeliveryJob INSTANCE = new OrderDeliveryJob();
    }

    public static OrderDeliveryJob getInstance() {
        return OrderDeliveryJobHelper.INSTANCE;
    }

    private OrderDeliveryJob() {
    }

    public void init() {
        long checkOrdersInterval = Long.parseLong(properties.getProperty("app.check-orders-interval-sec"));
        new Timer("deliveryJobTimer")
                .schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkOrdersAndDeliver();
                    }
                }, 1000 * checkOrdersInterval, 1000 * checkOrdersInterval);
    }

    private void checkOrdersAndDeliver() {
        List<Order> orders = orderDAO.getOrdersByStatus(COMPLETED);
        if (orders.isEmpty()) {
            log.info("No orders to deliver at this moment");
            return;
        }
        long timeToPrepareOrder = Long.parseLong(properties.getProperty("app.time-to-prepare-order-sec"));
        orders.forEach(order -> {
            if (ChronoUnit.SECONDS.between(order.getUpdatedAt(), LocalDateTime.now()) >= timeToPrepareOrder) {
                log.info("Delivering order: {}", order.getId());
                orderDAO.deleteOrder(order.getId());
            } else {
                log.info("Order is completed but not ready for delivery yet: {}", order.getId());
            }
        });
    }
}
