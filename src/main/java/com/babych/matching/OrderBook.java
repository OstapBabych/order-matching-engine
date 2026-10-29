package com.babych.matching;

import java.util.*;

public class OrderBook {
    private final TreeMap<Long, ArrayDeque<Order>> bids;
    private final TreeMap<Long, ArrayDeque<Order>> asks;

    public OrderBook() {
        this.bids = new TreeMap<>(Comparator.reverseOrder());
        this.asks = new TreeMap<>();
    }

    public void add(Order order) {
        Objects.requireNonNull(order, "order must not be null");

        TreeMap<Long, ArrayDeque<Order>> bookSide = (order.getSide() == Side.BUY) ? bids : asks;
        bookSide.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>()).add(order);
    }

    public OptionalLong bestBid() {
        if (bids.isEmpty()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(bids.firstKey());
    }

    public OptionalLong bestAsk() {
        if (asks.isEmpty()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(asks.firstKey());
    }

    public boolean isEmpty() {
        return bids.isEmpty() && asks.isEmpty();
    }
}
