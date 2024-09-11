package org.aivanouski.store.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.aivanouski.store.order.OrderValidator.validateDeliveryAddress;

public class Order {

    private UUID id;
    private int building;
    private int room;
    private OrderStatus status;
    private List<UUID> ingredients = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(UUID id, int building, int room, OrderStatus status, List<UUID> ingredients, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateDeliveryAddress(building, room);
        this.id = id;
        this.building = building;
        this.room = room;
        this.status = status;
        this.ingredients = ingredients;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private Order(Builder builder) {
        validateDeliveryAddress(builder.building, builder.room);
        this.id = builder.id;
        this.building = builder.building;
        this.room = builder.room;
        this.status = builder.status;
        this.ingredients = builder.ingredients;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getBuilding() {
        return building;
    }

    public void setBuilding(int building) {
        this.building = building;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<UUID> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<UUID> ingredients) {
        this.ingredients = ingredients;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return getBuilding() == order.getBuilding() && getRoom() == order.getRoom() && Objects.equals(getId(), order.getId()) && getStatus() == order.getStatus() && Objects.equals(getCreatedAt(), order.getCreatedAt()) && Objects.equals(getUpdatedAt(), order.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBuilding(), getRoom(), getStatus(), getCreatedAt(), getUpdatedAt());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", building=" + building +
                ", room=" + room +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static class Builder {
        private UUID id;
        private int building;
        private int room;
        private OrderStatus status;
        private List<UUID> ingredients = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Builder methods for setting fields
        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setBuilding(int building) {
            this.building = building;
            return this;
        }

        public Builder setRoom(int room) {
            this.room = room;
            return this;
        }

        public Builder setStatus(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder setIngredients(List<UUID> ingredients) {
            this.ingredients = ingredients;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
