package com.babych.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    MatchingEngine engine = new MatchingEngine(1000);

    @BeforeEach
    void startEngine(){
        engine.start();
    }

    @Test
    void orderWithoutMatchProducesNoTrades() throws Exception {
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        List<Trade> trades = future.get(1, TimeUnit.SECONDS);

        assertTrue(trades.isEmpty());
    }

    @Test
    void matchingOrdersProduceTrade() throws Exception {
        CompletableFuture<List<Trade>> futureBuy = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));
        futureBuy.get(1, TimeUnit.SECONDS);

        CompletableFuture<List<Trade>> futureSell = engine.submit(new Order(2, Side.SELL, 100, 50, 1001));
        List<Trade> trades = futureSell.get(1, TimeUnit.SECONDS);

        assertEquals(List.of(new Trade(1, 2, 100, 50)), trades);
    }

    @Test
    void duplicateIdErrorReachesClient() throws Exception{
        engine.submit(new Order(1, Side.SELL, 100, 50, 1000));
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(1, TimeUnit.SECONDS));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void matcherSurvivesFailedOrder() throws Exception{
        engine.submit(new Order(1, Side.SELL, 100, 50, 1000));
        CompletableFuture<List<Trade>> future = engine.submit(new Order(1, Side.BUY, 100, 50, 1000));

        assertThrows(ExecutionException.class, () -> future.get(1, TimeUnit.SECONDS));

        CompletableFuture<List<Trade>> next = engine.submit(new Order(2, Side.BUY, 100, 50, 1000));
        List<Trade> trades = next.get(1, TimeUnit.SECONDS);

        assertEquals(List.of(new Trade(2, 1, 100, 50)), trades);
    }

    @Test
    void startingTwiceThrows(){
        assertThrows(IllegalStateException.class, () -> engine.start());
    }

}