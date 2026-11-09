package com.babych.matching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    MatchingEngine engine = new MatchingEngine(1000);

    @BeforeEach
    void startEngine() {
        engine.start();
    }

    @AfterEach
    void stopEngine() throws InterruptedException {
        engine.stop();
    }

    @Test
    void orderWithoutMatchProducesNoTrades() throws Exception {
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        List<Trade> trades = future.get(1, SECONDS);

        assertTrue(trades.isEmpty());
    }

    @Test
    void matchingOrdersProduceTrade() throws Exception {
        CompletableFuture<List<Trade>> futureBuy = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));
        futureBuy.get(1, SECONDS);

        CompletableFuture<List<Trade>> futureSell = engine.submit(new Order(2, Side.SELL, 100, 50, 1001));
        List<Trade> trades = futureSell.get(1, SECONDS);

        assertEquals(List.of(new Trade(1, 2, 100, 50)), trades);
    }

    @Test
    void duplicateIdErrorReachesClient() throws Exception {
        engine.submit(new Order(1, Side.SELL, 100, 50, 1000));
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(1, SECONDS));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void matcherSurvivesFailedOrder() throws Exception {
        engine.submit(new Order(1, Side.SELL, 100, 50, 1000));
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        assertThrows(ExecutionException.class, () -> future.get(1, SECONDS));

        CompletableFuture<List<Trade>> next = engine.submit(new Order(2, Side.BUY, 100, 50, 1000));
        List<Trade> trades = next.get(1, SECONDS);

        assertEquals(List.of(new Trade(2, 1, 100, 50)), trades);
    }

    @Test
    void startingTwiceThrows() {
        assertThrows(IllegalStateException.class, () -> engine.start());
    }

    @Test
    void stopTerminatesMatcherThread() throws InterruptedException {
        assertTrue(engine.isRunning());

        engine.stop();
        assertFalse(engine.isRunning());
    }

    @Test
    void submitAfterStopThrows() throws InterruptedException {
        engine.stop();
        assertThrows(IllegalStateException.class, () -> engine.submit(new Order(1, Side.BUY, 10, 100, 1000)));
    }

    @Test
    void stopTwiceIsSafe() throws InterruptedException {
        engine.stop();
        assertDoesNotThrow(() -> engine.stop());
    }

    @Test
    void pendingOrdersFailOnStop() throws Exception {
        MatchingEngine engine1 = new MatchingEngine(1000);

        CompletableFuture<List<Trade>> first = engine1.submit(new Order(1, Side.BUY, 10, 100, 1000));
        CompletableFuture<List<Trade>> second = engine1.submit(new Order(2, Side.BUY, 10, 100, 1001));
        CompletableFuture<List<Trade>> third = engine1.submit(new Order(3, Side.BUY, 10, 100, 1002));

        engine1.stop();

        for (CompletableFuture<List<Trade>> future : List.of(first, second, third)) {
            ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(1, SECONDS));
            assertInstanceOf(IllegalStateException.class, exception.getCause());
        }
    }

    @Test
    void cancelRestingOrder() throws InterruptedException, ExecutionException, TimeoutException {
        engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        assertTrue(engine.cancel(1).get(1, SECONDS));
    }

    @Test
    void cancelUnknownOrderReturnsFalse() throws InterruptedException, ExecutionException, TimeoutException {
        assertFalse(engine.cancel(999).get(1, SECONDS));
    }

    @Test
    void cancelledOrderDoesNotMatch() throws InterruptedException, ExecutionException, TimeoutException {
        engine.submit(new Order(1, Side.SELL, 100, 50, 1000));

        assertTrue(engine.cancel(1).get(1, SECONDS));

        List<Trade> trades = engine.submit(new Order(2, Side.BUY, 100, 50, 1001)).get(1, SECONDS);

        assertTrue(trades.isEmpty());
    }

    @Test
    void cancelAfterStopThrows() throws InterruptedException {
        engine.stop();

        assertThrows(IllegalStateException.class, () -> engine.cancel(1));
    }
}