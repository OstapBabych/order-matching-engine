package com.babych.matching;

public record Trade(long buyOrderId, long sellOrderId, long price, long quantity) {
    public Trade {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive, got: " + price);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, got: " + quantity);
        }
    }
}

