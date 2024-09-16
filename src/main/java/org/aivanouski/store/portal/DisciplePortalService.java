package org.aivanouski.store.portal;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.aivanouski.store.ingredient.Ingredient;
import org.aivanouski.store.ingredient.IngredientDAO;
import org.aivanouski.store.order.Order;
import org.aivanouski.store.order.OrderDAO;
import org.aivanouski.store.order.OrderStatus;
import org.aivanouski.store.proto.DisciplePortal;
import org.aivanouski.store.proto.DiscipleServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.aivanouski.store.ingredient.IngredientValidator.validateIngredients;
import static org.aivanouski.store.order.OrderStatus.IN_PROGRESS;
import static org.aivanouski.store.order.OrderValidator.validateOrderBeforeDelivery;

public class DisciplePortalService extends DiscipleServiceGrpc.DiscipleServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DisciplePortalService.class);

    private static final String OK_MESSAGE = "OK";

    private final OrderDAO orderDAO;
    private final IngredientDAO ingredientDAO;

    public DisciplePortalService(OrderDAO orderDAO, IngredientDAO ingredientDAO) {
        this.orderDAO = orderDAO;
        this.ingredientDAO = ingredientDAO;
    }

    @Override
    public void createOrder(DisciplePortal.OrderCreateRequest request, StreamObserver<DisciplePortal.OrderResponse> responseObserver) {
        DisciplePortal.OrderResponse.Builder response = DisciplePortal.OrderResponse.newBuilder();
        DisciplePortal.Metadata.Builder metadata = DisciplePortal.Metadata.newBuilder();
        try {
            Order order = new Order.Builder()
                    .setBuilding(request.getBuilding())
                    .setRoom(request.getRoom())
                    .setStatus(IN_PROGRESS)
                    .build();
            order = orderDAO.createOrder(order);
            response
                    .setBody(DisciplePortal.OrderResponseBody.newBuilder()
                            .setId(order.getId().toString())
                            .setBuilding(order.getBuilding())
                            .setRoom(order.getRoom())
                            .setStatus(order.getStatus().name())
                            .addAllIngredients(order.getIngredients().stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.toList()))
                            .setCreatedAt(Timestamp.newBuilder()
                                    .setSeconds(order.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                                    .setNanos(order.getCreatedAt().getNano())
                                    .build())
                            .setUpdatedAt(Timestamp.newBuilder()
                                    .setSeconds(order.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
                                    .setNanos(order.getUpdatedAt().getNano())
                                    .build())
                            .build())
                    .setMetadata(metadata
                            .setRet(0)
                            .setMessage(OK_MESSAGE)
                            .build());
        } catch (Exception e) {
            log.error("Error creating order", e);
            response.setMetadata(metadata
                    .setRet(1)
                    .setMessage("Error creating order: " + e.getMessage())
                    .build()
            );
        } finally {
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getIngredients(DisciplePortal.EmptyRequest request, StreamObserver<DisciplePortal.IngredientsResponse> responseObserver) {
        DisciplePortal.IngredientsResponse.Builder response = DisciplePortal.IngredientsResponse.newBuilder();
        DisciplePortal.Metadata.Builder metadata = DisciplePortal.Metadata.newBuilder();
        try {
            List<Ingredient> ingredients = ingredientDAO.getAllIngredients();
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("No ingredients found");
            }
            response
                    .setBody(DisciplePortal.IngredientsResponseBody.newBuilder()
                            .addAllItems(ingredients.stream()
                                    .map(ingredient -> DisciplePortal.IngredientItem.newBuilder()
                                            .setId(ingredient.getId().toString())
                                            .setName(ingredient.getName())
                                            .build())
                                    .collect(Collectors.toList())))
                    .setMetadata(metadata
                            .setRet(0)
                            .setMessage(OK_MESSAGE)
                            .build());
        } catch (Exception e) {
            log.error("Error getting ingredients", e);
            response.setMetadata(metadata
                    .setRet(1)
                    .setMessage("Error getting ingredients: " + e.getMessage())
                    .build()
            );
        } finally {
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void addPancakes(DisciplePortal.AddPancakesRequest request, StreamObserver<DisciplePortal.EmptyResponse> responseObserver) {
        DisciplePortal.EmptyResponse.Builder response = DisciplePortal.EmptyResponse.newBuilder();
        DisciplePortal.Metadata.Builder metadata = DisciplePortal.Metadata.newBuilder();
        try {
            validateIngredients(request.getIngredientsList(), ingredientDAO.getAllIngredients());

            orderDAO.addIngredients(
                    UUID.fromString(request.getOrderId()),
                    request.getIngredientsList().stream().map(UUID::fromString).collect(Collectors.toList())
            );
            response
                    .setMetadata(metadata
                            .setRet(0)
                            .setMessage(OK_MESSAGE));
        } catch (Exception e) {
            log.error("Error adding pancakes", e);
            response.setMetadata(metadata
                    .setRet(1)
                    .setMessage("Error adding pancakes: " + e.getMessage())
                    .build()
            );
        } finally {
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void completeOrder(DisciplePortal.CompleteOrderRequest request, StreamObserver<DisciplePortal.OrderResponse> responseObserver) {
        DisciplePortal.OrderResponse.Builder response = DisciplePortal.OrderResponse.newBuilder();
        DisciplePortal.Metadata.Builder metadata = DisciplePortal.Metadata.newBuilder();
        try {
            if (DisciplePortal.CompleteOrderRequest.Status.UNKNOWN == request.getStatus()) {
                throw new IllegalArgumentException(
                        String.format("Unknown target order status, please specify %s or %s",
                                DisciplePortal.CompleteOrderRequest.Status.COMPLETED,
                                DisciplePortal.CompleteOrderRequest.Status.CANCELLED));
            }
            Order order = orderDAO.getOrderById(UUID.fromString(request.getId()));
            if (DisciplePortal.CompleteOrderRequest.Status.CANCELLED == request.getStatus()) {
                orderDAO.deleteOrder(order.getId());
                response.setMetadata(
                        metadata
                                .setRet(0)
                                .setMessage("Order cancelled and was deleted from the system")
                                .build()
                );
                return;
            } else if (DisciplePortal.CompleteOrderRequest.Status.COMPLETED == request.getStatus()) {
                validateOrderBeforeDelivery(order);
                order.setStatus(OrderStatus.valueOf(request.getStatus().name()));
                order = orderDAO.updateOrder(order);
                metadata
                        .setRet(0)
                        .setMessage("Order completed and passed to chef");
            }
            response
                    .setBody(DisciplePortal.OrderResponseBody.newBuilder()
                            .setId(order.getId().toString())
                            .setBuilding(order.getBuilding())
                            .setRoom(order.getRoom())
                            .setStatus(order.getStatus().name())
                            .addAllIngredients(order.getIngredients().stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.toList()))
                            .setCreatedAt(Timestamp.newBuilder()
                                    .setSeconds(order.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                                    .setNanos(order.getCreatedAt().getNano())
                                    .build())
                            .setUpdatedAt(Timestamp.newBuilder()
                                    .setSeconds(order.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
                                    .setNanos(order.getUpdatedAt().getNano())
                                    .build())
                    )
                    .setMetadata(metadata.build());
        } catch (Exception e) {
            log.error("Error completing order", e);
            response.setMetadata(metadata
                    .setRet(1)
                    .setMessage("Error completing order: " + e.getMessage())
                    .build()
            );
        } finally {
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }
}
