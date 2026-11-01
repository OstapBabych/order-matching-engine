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

        TreeMap<Long, ArrayDeque<Order>> bookSide = levelsFor(order.getSide());
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

    public List<Trade> submit(Order incoming) {
        Objects.requireNonNull(incoming, "order must not be null");
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
            }
            if (queue.isEmpty()) {
                opposite.pollFirstEntry();
            }

        }

        if (!incoming.isFilled()) {
            add(incoming);
        }

        return trades;
    }

    private TreeMap<Long, ArrayDeque<Order>> levelsFor(Side side) {
        return (side == Side.BUY) ? bids : asks;
    }

    private boolean crosses(Order incoming, long restingPrice) {
        return (incoming.getSide() == Side.BUY)
                ? incoming.getPrice() >= restingPrice
                : incoming.getPrice() <= restingPrice;
    }
}
