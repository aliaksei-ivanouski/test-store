package org.aivanouski.store.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.aivanouski.store.order.OrderStatus.COMPLETED;

public class OrderDeliveryJob {

    private static final Logger log = LoggerFactory.getLogger(OrderDeliveryJob.class);

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
        new Timer("deliveryJobTimer")
                .schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkOrdersAndDeliver();
                    }
                }, 1000 * 10, 1000 * 10);
    }

    private void checkOrdersAndDeliver() {
        List<Order> orders = orderDAO.getOrdersByStatus(COMPLETED);
        if (orders.isEmpty()) {
            log.info("No orders to deliver at this moment");
            return;
        }
        orders.forEach(order -> {
            if (ChronoUnit.SECONDS.between(order.getUpdatedAt(), LocalDateTime.now()) > 30) {
                log.info("Delivering order: {}", order.getId());
                orderDAO.deleteOrder(order.getId());
            } else {
                log.info("Order is completed but not ready for delivery yet: {}", order.getId());
            }
        });
    }
}
