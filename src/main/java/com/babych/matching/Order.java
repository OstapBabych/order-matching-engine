package com.babych.matching;

import java.util.Objects;

public class Order {

    private final long id;
    private final Side side;
    private final long price;
    private final long originalQuantity;
    private long remainingQuantity;
    private final long timestamp;

    public Order(long id, Side side, long price, long quantity, long timestamp) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive, got: " + id);
        }
        this.id = id;
        this.side = Objects.requireNonNull(side, "side must not be null");

        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive, got: " + price);
        }
        this.price = price;

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, got: " + quantity);
        }
        this.originalQuantity = quantity;

        this.remainingQuantity = quantity;
        this.timestamp = timestamp;
    }


    public long getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public long getPrice() {
        return price;
    }

    public long getOriginalQuantity() {
        return originalQuantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void fill(long quantity) {
        if (isFilled()) {
            throw new IllegalStateException("order id: " + id + " - is filled");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, got: " + quantity);
        }
        if (remainingQuantity < quantity) {
            throw new IllegalArgumentException("fill quantity " + quantity + " exceeds remaining " + remainingQuantity + " for order id: " + id);
        }
        this.remainingQuantity -= quantity;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", side=" + side +
                ", price=" + price +
                ", quantity=" + remainingQuantity + "/" + originalQuantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
