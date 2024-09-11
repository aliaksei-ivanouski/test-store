package org.aivanouski.store.portal;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.aivanouski.store.config.PropertiesConfig;
import org.aivanouski.store.ingredient.Ingredient;
import org.aivanouski.store.ingredient.IngredientDAOImpl;
import org.aivanouski.store.order.Order;
import org.aivanouski.store.order.OrderDAOImpl;
import org.aivanouski.store.order.OrderStatus;
import org.aivanouski.store.proto.DisciplePortal;
import org.aivanouski.store.proto.DiscipleServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.aivanouski.store.order.OrderStatus.IN_PROGRESS;
import static org.aivanouski.store.proto.DisciplePortal.CompleteOrderRequest.Status.CANCELLED;
import static org.aivanouski.store.proto.DisciplePortal.CompleteOrderRequest.Status.COMPLETED;
import static org.aivanouski.store.proto.DisciplePortal.CompleteOrderRequest.Status.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisciplePortalServiceTest {

    private ManagedChannel channel;
    private Server server;
    private DiscipleServiceGrpc.DiscipleServiceBlockingStub stub;

    private OrderDAOImpl orderDAO;
    private IngredientDAOImpl ingredientDAO;

    @BeforeEach
    void setUp() throws IOException {
        PropertiesConfig.getInstance().init();

        orderDAO = mock(OrderDAOImpl.class);
        ingredientDAO = mock(IngredientDAOImpl.class);
        DisciplePortalService service = new DisciplePortalService(orderDAO, ingredientDAO);
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .addService(service)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(serverName)
                .usePlaintext()
                .directExecutor()
                .build();
        stub = DiscipleServiceGrpc.newBlockingStub(channel);
    }

    @Test
    void createOrderTest() {
        // given
        LocalDateTime time = LocalDateTime.now();
        final UUID id = UUID.randomUUID();
        Order order = new Order.Builder()
                .setId(id)
                .setBuilding(10)
                .setRoom(111)
                .setStatus(IN_PROGRESS)
                .setCreatedAt(time)
                .setUpdatedAt(time)
                .build();

        // when
        when(orderDAO.createOrder(any(Order.class))).thenReturn(order);

        DisciplePortal.OrderResponse response = stub.createOrder(
                DisciplePortal.OrderCreateRequest.newBuilder()
                        .setBuilding(10)
                        .setRoom(111)
                        .build()
        );

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(0, metadata.getRet());
        assertEquals("OK", metadata.getMessage());
        DisciplePortal.OrderResponseBody body = response.getBody();
        assertEquals(id.toString(), body.getId());
        assertEquals(10, body.getBuilding());
        assertEquals(111, body.getRoom());
        assertEquals("IN_PROGRESS", body.getStatus());
        assertNotNull(body.getCreatedAt());
        assertEquals(time.getNano(), body.getCreatedAt().getNanos());
        assertEquals(time.toEpochSecond(ZoneOffset.UTC), body.getCreatedAt().getSeconds());
        assertNotNull(body.getUpdatedAt());
        assertEquals(time.getNano(), body.getUpdatedAt().getNanos());
        assertEquals(time.toEpochSecond(ZoneOffset.UTC), body.getUpdatedAt().getSeconds());
        assertTrue(body.getIngredientsList().isEmpty());
    }

    @Test
    void createOrderTest_invalidBuilding() {
        DisciplePortal.OrderResponse response = stub.createOrder(
                DisciplePortal.OrderCreateRequest.newBuilder()
                        .setBuilding(96)
                        .setRoom(111)
                        .build()
        );
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error creating order: Building and room must not exceed the following values: " +
                "building: 50 and room 120", metadata.getMessage());
    }

    @Test
    void createOrderTest_invalidRoom() {
        DisciplePortal.OrderResponse response = stub.createOrder(
                DisciplePortal.OrderCreateRequest.newBuilder()
                        .setBuilding(10)
                        .setRoom(189)
                        .build()
        );
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error creating order: Building and room must not exceed the following values: " +
                "building: 50 and room 120", metadata.getMessage());
    }

    @Test
    void getIngredientsTest() {
        // given
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient.Builder().setId(UUID.fromString("2fd86376-c492-4c16-8665-881cf47c96ca")).setName("dark chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("bda44844-23ad-4487-ac9d-1849b2bfd9ff")).setName("milk chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("71251065-1134-4808-b47e-38320f3c307d")).setName("whipped cream").build(),
                new Ingredient.Builder().setId(UUID.fromString("22595a60-3fe4-47e3-8482-4140e10f696f")).setName("hazelnuts").build()
        );

        // when
        when(ingredientDAO.getAllIngredients()).thenReturn(ingredients);
        DisciplePortal.IngredientsResponse response
                = stub.getIngredients(DisciplePortal.EmptyRequest.newBuilder().build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(0, metadata.getRet());
        assertEquals("OK", metadata.getMessage());
        DisciplePortal.IngredientsResponseBody body = response.getBody();
        assertEquals(4, body.getItemsCount());
        assertEquals("dark chocolate", body.getItems(0).getName());
        assertEquals("2fd86376-c492-4c16-8665-881cf47c96ca", body.getItems(0).getId());
        assertEquals("milk chocolate", body.getItems(1).getName());
        assertEquals("bda44844-23ad-4487-ac9d-1849b2bfd9ff", body.getItems(1).getId());
        assertEquals("whipped cream", body.getItems(2).getName());
        assertEquals("71251065-1134-4808-b47e-38320f3c307d", body.getItems(2).getId());
        assertEquals("hazelnuts", body.getItems(3).getName());
        assertEquals("22595a60-3fe4-47e3-8482-4140e10f696f", body.getItems(3).getId());
    }

    @Test
    void getIngredientsTest_ingredientsEmpty() {
        // given

        // when
        when(ingredientDAO.getAllIngredients()).thenReturn(Collections.emptyList());
        DisciplePortal.IngredientsResponse response
                = stub.getIngredients(DisciplePortal.EmptyRequest.newBuilder().build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error getting ingredients: No ingredients found", metadata.getMessage());
    }

    @Test
    void getIngredientsTest_exception() {
        // given

        // when
        when(ingredientDAO.getAllIngredients()).thenThrow(new RuntimeException("Some message"));
        DisciplePortal.IngredientsResponse response
                = stub.getIngredients(DisciplePortal.EmptyRequest.newBuilder().build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error getting ingredients: Some message", metadata.getMessage());
    }

    @Test
    void addPancakesTest() {
        // given
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient.Builder().setId(UUID.fromString("2fd86376-c492-4c16-8665-881cf47c96ca")).setName("dark chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("bda44844-23ad-4487-ac9d-1849b2bfd9ff")).setName("milk chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("71251065-1134-4808-b47e-38320f3c307d")).setName("whipped cream").build(),
                new Ingredient.Builder().setId(UUID.fromString("22595a60-3fe4-47e3-8482-4140e10f696f")).setName("hazelnuts").build()
        );

        // when
        when(ingredientDAO.getAllIngredients()).thenReturn(ingredients);
        doNothing().when(orderDAO).addIngredients(any(UUID.class), any(List.class));
        DisciplePortal.EmptyResponse response
                = stub.addPancakes(DisciplePortal.AddPancakesRequest.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .addAllIngredients(ingredients.stream().map(Ingredient::getId).map(UUID::toString).collect(Collectors.toList()))
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(0, metadata.getRet());
        assertEquals("OK", metadata.getMessage());
    }

    @Test
    void addPancakesTest_illegalIngredients() {
        // given
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient.Builder().setId(UUID.fromString("2fd86376-c492-4c16-8665-881cf47c96ca")).setName("dark chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("bda44844-23ad-4487-ac9d-1849b2bfd9ff")).setName("milk chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("71251065-1134-4808-b47e-38320f3c307d")).setName("whipped cream").build(),
                new Ingredient.Builder().setId(UUID.fromString("22595a60-3fe4-47e3-8482-4140e10f696f")).setName("hazelnuts").build()
        );

        // when
        when(ingredientDAO.getAllIngredients()).thenReturn(ingredients);
        doNothing().when(orderDAO).addIngredients(any(UUID.class), any(List.class));
        DisciplePortal.EmptyResponse response
                = stub.addPancakes(DisciplePortal.AddPancakesRequest.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .addAllIngredients(Arrays.asList("9e90f7c7-9cc0-433a-bb1b-f5d4bc357782", "c34b02c5-fb76-4836-b106-093a3edc46ab"))
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error adding pancakes: Illegal ingredients found: 9e90f7c7-9cc0-433a-bb1b-f5d4bc357782, c34b02c5-fb76-4836-b106-093a3edc46ab", metadata.getMessage());
    }

    @Test
    void addPancakesTest_exception() {
        // given
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient.Builder().setId(UUID.fromString("2fd86376-c492-4c16-8665-881cf47c96ca")).setName("dark chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("bda44844-23ad-4487-ac9d-1849b2bfd9ff")).setName("milk chocolate").build(),
                new Ingredient.Builder().setId(UUID.fromString("71251065-1134-4808-b47e-38320f3c307d")).setName("whipped cream").build(),
                new Ingredient.Builder().setId(UUID.fromString("22595a60-3fe4-47e3-8482-4140e10f696f")).setName("hazelnuts").build()
        );

        // when
        when(ingredientDAO.getAllIngredients()).thenReturn(ingredients);
        doThrow(new RuntimeException("Some message")).when(orderDAO).addIngredients(any(UUID.class), any(List.class));
        DisciplePortal.EmptyResponse response
                = stub.addPancakes(DisciplePortal.AddPancakesRequest.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .addAllIngredients(ingredients.stream().map(Ingredient::getId).map(UUID::toString).collect(Collectors.toList()))
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals("Error adding pancakes: Some message", metadata.getMessage());
    }

    @Test
    void completeOrderTest() {
        // given
        List<UUID> ingredientIds = Arrays.asList(
                UUID.fromString("bda44844-23ad-4487-ac9d-1849b2bfd9ff"),
                UUID.fromString("22595a60-3fe4-47e3-8482-4140e10f696f")
        );

        LocalDateTime time = LocalDateTime.now();
        final UUID id = UUID.randomUUID();
        Order order = new Order.Builder()
                .setId(id)
                .setBuilding(10)
                .setRoom(111)
                .setStatus(IN_PROGRESS)
                .setIngredients(ingredientIds)
                .setCreatedAt(time)
                .setUpdatedAt(time)
                .build();

        Order updatedOrder = new Order.Builder()
                .setId(id)
                .setBuilding(10)
                .setRoom(111)
                .setStatus(OrderStatus.COMPLETED)
                .setIngredients(ingredientIds)
                .setCreatedAt(time)
                .setUpdatedAt(time)
                .build();

        // when
        when(orderDAO.getOrderById(any(UUID.class))).thenReturn(order);
        when(orderDAO.updateOrder(any(Order.class))).thenReturn(updatedOrder);
        doNothing().when(orderDAO).deleteOrder(any(UUID.class));
        DisciplePortal.OrderResponse response = stub.completeOrder(DisciplePortal.CompleteOrderRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setStatus(COMPLETED)
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(0, metadata.getRet());
        assertEquals("Order completed and passed to chef", metadata.getMessage());

        DisciplePortal.OrderResponseBody body = response.getBody();
        assertEquals(id.toString(), body.getId());
        assertEquals(10, body.getBuilding());
        assertEquals(111, body.getRoom());
        assertEquals("COMPLETED", body.getStatus());
        assertNotNull(body.getCreatedAt());
        assertEquals(time.getNano(), body.getCreatedAt().getNanos());
        assertEquals(time.toEpochSecond(ZoneOffset.UTC), body.getCreatedAt().getSeconds());
        assertNotNull(body.getUpdatedAt());
        assertEquals(time.getNano(), body.getUpdatedAt().getNanos());
        assertEquals(time.toEpochSecond(ZoneOffset.UTC), body.getUpdatedAt().getSeconds());
        assertFalse(body.getIngredientsList().isEmpty());
        assertTrue(body.getIngredientsList().containsAll(ingredientIds.stream().map(UUID::toString).collect(Collectors.toList())));
    }

    @Test
    void completeOrderTest_cancelledStatus() {
        // given

        // when
        when(orderDAO.getOrderById(any(UUID.class))).thenReturn(new Order.Builder().build());
        doNothing().when(orderDAO).deleteOrder(any(UUID.class));
        DisciplePortal.OrderResponse response = stub.completeOrder(DisciplePortal.CompleteOrderRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setStatus(CANCELLED)
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(0, metadata.getRet());
        assertEquals("Order cancelled and was deleted from the system", metadata.getMessage());
    }

    @Test
    void completeOrderTest_unknownStatus() {
        // given

        // when
        DisciplePortal.OrderResponse response = stub.completeOrder(DisciplePortal.CompleteOrderRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setStatus(UNKNOWN)
                .build());

        // then
        DisciplePortal.Metadata metadata = response.getMetadata();
        assertEquals(1, metadata.getRet());
        assertEquals(String.format("Error completing order: Unknown target order status, please specify %s or %s",
                DisciplePortal.CompleteOrderRequest.Status.COMPLETED,
                DisciplePortal.CompleteOrderRequest.Status.CANCELLED), metadata.getMessage());
    }

    @AfterEach
    public void tearDown() {
        // Shutdown server and channel after each test
        channel.shutdownNow();
        server.shutdownNow();
    }
}
