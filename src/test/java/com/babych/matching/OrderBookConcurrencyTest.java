package com.babych.matching;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookConcurrencyTest {
    @Test
    void concurrentSubmitsNeverFillTheSameOrderTwice() throws Exception {
        int threads = 8;
        int ordersPerThread = 1250;
        int totalSells = threads * ordersPerThread;

        OrderBook orderBook = new OrderBook();

        for (int id = 1; id <= totalSells; id++) {
            orderBook.submit(new Order(id, Side.SELL, 100, 1, 0));
        }

        AtomicLong nextId = new AtomicLong(100000000);
        Queue<Trade> allTrades = new ConcurrentLinkedQueue<>();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < ordersPerThread; i++) {
                    Order buy = Order.market(nextId.getAndIncrement(), Side.BUY, 1, 0);
                    allTrades.addAll(orderBook.submit(buy));
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        Set<Long> soldIds = new HashSet<>();
        for (Trade trade : allTrades) {
            soldIds.add(trade.sellOrderId());
        }
        assertEquals(totalSells, soldIds.size());
        assertEquals(totalSells, allTrades.size());
        assertTrue(orderBook.isEmpty());
    }

}
