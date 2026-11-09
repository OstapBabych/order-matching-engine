package com.babych.matching;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingEngineConcurrencyTest {
    @Test
    void concurrentSubmitsNeverFillTheSameOrderTwice() throws Exception {
        int threads = 8;
        int ordersPerThread = 1250;
        int totalSells = threads * ordersPerThread;
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        MatchingEngine engine = new MatchingEngine(10000);
        engine.start();

        try {
            for (int id = 1; id <= totalSells; id++) {
                engine.submit(new Order(id, Side.SELL, 100, 1, 0));
            }

            AtomicLong nextId = new AtomicLong(1000000);
            Queue<Trade> allTrades = new ConcurrentLinkedQueue<>();
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < ordersPerThread; i++) {
                        Order buy = Order.market(nextId.getAndIncrement(), Side.BUY, 1, 0);
                        allTrades.addAll(engine.submit(buy).get(1, TimeUnit.SECONDS));
                    }
                    return null;
                }));
            }

            start.countDown();
            for (Future<?> f : futures) {
                f.get();
            }

            Set<Long> soldIds = new HashSet<>();
            Set<Long> boughtIds = new HashSet<>();
            for (Trade trade : allTrades) {
                soldIds.add(trade.sellOrderId());
                boughtIds.add(trade.buyOrderId());
            }

            assertEquals(totalSells, soldIds.size());
            assertEquals(totalSells, boughtIds.size());
            assertEquals(totalSells, allTrades.size());

        } finally {
            pool.shutdown();
            engine.stop();
        }
    }
}
