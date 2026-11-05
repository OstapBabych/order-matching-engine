package com.babych.matching;

import java.util.*;

public class OrderBook {
    private final TreeMap<Long, ArrayDeque<Order>> bids;
    private final TreeMap<Long, ArrayDeque<Order>> asks;
    private final HashMap<Long, Order> orders;

    public OrderBook() {
        this.bids = new TreeMap<>(Comparator.reverseOrder());
        this.asks = new TreeMap<>();
        this.orders = new HashMap<>();
    }

    private void add(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        if (orders.containsKey(order.getId())) {
            throw new IllegalArgumentException("order with id: " + order.getId() + " already exists");
        }

        TreeMap<Long, ArrayDeque<Order>> bookSide = levelsFor(order.getSide());
        bookSide.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>()).add(order);

        orders.put(order.getId(), order);
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

    public List<Trade> submit(Order incoming) {
        Objects.requireNonNull(incoming, "order must not be null");

        if (orders.containsKey(incoming.getId())) {
            throw new IllegalArgumentException("order with id: " + incoming.getId() + " already exists");
        }

        List<Trade> trades = new ArrayList<>();
        TreeMap<Long, ArrayDeque<Order>> opposite = levelsFor(incoming.getSide().opposite());

        while (!incoming.isFilled() && !opposite.isEmpty() && crosses(incoming, opposite.firstKey())) {
            ArrayDeque<Order> queue = opposite.firstEntry().getValue();
            Order resting = queue.peekFirst();

            long quantity = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());

            incoming.fill(quantity);
            resting.fill(quantity);

            if (incoming.getSide() == Side.BUY) {
                trades.add(new Trade(incoming.getId(), resting.getId(), resting.getPrice(), quantity));
            } else {
                trades.add(new Trade(resting.getId(), incoming.getId(), resting.getPrice(), quantity));
            }

            if (resting.isFilled()) {
                queue.pollFirst();
                orders.remove(resting.getId());
            }
            if (queue.isEmpty()) {
                opposite.pollFirstEntry();
            }

        }

        if (!incoming.isFilled() && incoming.getOrderType() == OrderType.LIMIT) {
            add(incoming);
        }

        return trades;
    }

    public boolean cancel(long orderId) {
        Order order = orders.get(orderId);

        if (order == null) {
            return false;
        }

        TreeMap<Long, ArrayDeque<Order>> levels = levelsFor(order.getSide());
        ArrayDeque<Order> queue = levels.get(order.getPrice());
        queue.remove(order);

        if (queue.isEmpty()) {
            levels.remove(order.getPrice());
        }

        orders.remove(orderId);

        return true;
    }

    private TreeMap<Long, ArrayDeque<Order>> levelsFor(Side side) {
        return (side == Side.BUY) ? bids : asks;
    }

    private boolean crosses(Order incoming, long restingPrice) {
        if (incoming.getOrderType() == OrderType.MARKET) {
            return true;
        }
        return (incoming.getSide() == Side.BUY)
                ? incoming.getPrice() >= restingPrice
                : incoming.getPrice() <= restingPrice;
    }
}
