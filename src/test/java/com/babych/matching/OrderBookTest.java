package com.babych.matching;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    OrderBook orderBook = new OrderBook();
    Order order11 = new Order(1, Side.BUY, 10, 100, 1000);
    Order order21 = new Order(2, Side.SELL, 10, 100, 1000);
    Order order12 = new Order(3, Side.BUY, 15, 100, 1000);
    Order order22 = new Order(4, Side.SELL, 15, 100, 1000);
    Order order13 = new Order(5, Side.BUY, 5, 100, 1000);
    Order order23 = new Order(6, Side.SELL, 5, 100, 1000);

    @Test
    void newBookIsEmpty() {
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void emptyBookHasNoBestBidOrAsk() {
        assertTrue(orderBook.bestAsk().isEmpty());
        assertTrue(orderBook.bestBid().isEmpty());
    }

    @Test
    void addingBuyOrderSetsBestBid() {
        orderBook.submit(order11);

        assertEquals(OptionalLong.of(10), orderBook.bestBid());
    }

    @Test
    void addingSellOrderSetsBestAsk() {
        orderBook.submit(order21);

        assertEquals(OptionalLong.of(10), orderBook.bestAsk());
    }

    @Test
    void bestBidIsHighestBuyPrice() {
        orderBook.submit(order11);
        orderBook.submit(order12);
        orderBook.submit(order13);

        assertEquals(OptionalLong.of(15), orderBook.bestBid());
    }

    @Test
    void bestAskIsLowestSellPrice() {
        orderBook.submit(order21);
        orderBook.submit(order22);
        orderBook.submit(order23);

        assertEquals(OptionalLong.of(5), orderBook.bestAsk());
    }

    @Test
    void buyAndSellAreStoredSeparately() {
        orderBook.submit(order11);
        orderBook.submit(order22);

        assertEquals(OptionalLong.of(10), orderBook.bestBid());
        assertEquals(OptionalLong.of(15), orderBook.bestAsk());
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(NullPointerException.class, () -> orderBook.submit(null));
    }

    @Test
    void fullMatchAtSamePriceProducesOneTrade() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 50)), trades);
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void noMatchWhenPricesDoNotCross() {
        orderBook.submit(new Order(1, Side.SELL, 105, 50, 1000));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertTrue(trades.isEmpty());
        assertEquals(OptionalLong.of(100), orderBook.bestBid());
        assertEquals(OptionalLong.of(105), orderBook.bestAsk());
    }

    @Test
    void partialFillLeavesRemainderOfIncomingInBook() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 30)), trades);
        assertEquals(OptionalLong.of(100), orderBook.bestBid());
        assertEquals(OptionalLong.empty(), orderBook.bestAsk());
    }

    @Test
    void partialFillLeavesRemainderOfRestingInBook() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 100, 30, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 30)), trades);
        assertEquals(OptionalLong.empty(), orderBook.bestBid());
        assertEquals(OptionalLong.of(100), orderBook.bestAsk());
    }

    @Test
    void tradeExecutesAtRestingPrice() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 105, 50, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 50)), trades);
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void sellIncomingMatchesBids() {
        orderBook.submit(new Order(2, Side.BUY, 100, 50, 1000));

        List<Trade> trades = orderBook.submit(new Order(1, Side.SELL, 100, 50, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 50)), trades);
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void earlierOrderAtSamePriceFillsFirst() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));
        orderBook.submit(new Order(2, Side.SELL, 100, 30, 1001));

        List<Trade> trades = orderBook.submit(new Order(3, Side.BUY, 100, 30, 1002));

        assertEquals(List.of(new Trade(3, 1, 100, 30)), trades);
        assertEquals(OptionalLong.of(100), orderBook.bestAsk());
    }

    @Test
    void incomingSweepsMultiplePriceLevels() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));
        orderBook.submit(new Order(2, Side.SELL, 101, 30, 1001));

        List<Trade> trades = orderBook.submit(new Order(3, Side.BUY, 101, 50, 1002));

        assertEquals(List.of(new Trade(3, 1, 100, 30), new Trade(3, 2, 101, 20)), trades);
        assertEquals(OptionalLong.of(101), orderBook.bestAsk());
    }

    @Test
    void cancelRemovesOrderFromBook() {
        orderBook.submit(new Order(1, Side.BUY, 100, 50, 1000));

        assertTrue(orderBook.cancel(1));
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void cancelUnknownIdReturnsFalse() {
        assertFalse(orderBook.cancel(1));
    }

    @Test
    void cancelTwiceReturnsFalseSecondTime() {
        orderBook.submit(new Order(1, Side.BUY, 100, 50, 1000));

        assertTrue(orderBook.cancel(1));
        assertFalse(orderBook.cancel(1));
    }

    @Test
    void cancelKeepsOtherOrdersAtSameLevel() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));
        orderBook.submit(new Order(2, Side.SELL, 100, 50, 1001));

        assertTrue(orderBook.cancel(1));

        List<Trade> trades = orderBook.submit(new Order(3, Side.BUY, 100, 50, 1002));

        assertEquals(List.of(new Trade(3, 2, 100, 50)), trades);
    }

    @Test
    void cancelledOrderIsNotMatched() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        assertTrue(orderBook.cancel(1));

        List<Trade> trades = orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertTrue(trades.isEmpty());
        assertEquals(OptionalLong.of(100), orderBook.bestBid());
    }

    @Test
    void filledOrderCannotBeCancelled() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertFalse(orderBook.cancel(1));
    }

    @Test
    void partiallyFilledIncomingCanBeCancelled() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));

        orderBook.submit(new Order(2, Side.BUY, 100, 50, 1001));

        assertTrue(orderBook.cancel(2));
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void rejectsDuplicateOrderId() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));

        assertThrows(IllegalArgumentException.class, () -> orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000)));
    }

    @Test
    void marketBuyMatchesBestAsk() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));

        List<Trade> trades = orderBook.submit(Order.market(2, Side.BUY, 30, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 30)), trades);
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void marketOrderSweepsAnyPrice() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1001));
        orderBook.submit(new Order(2, Side.SELL, 105, 30, 1000));

        List<Trade> trades = orderBook.submit(Order.market(3, Side.BUY, 50, 1002));

        assertEquals(List.of(new Trade(3, 1, 100, 30),
                new Trade(3, 2, 105, 20)), trades);
        assertEquals(OptionalLong.of(105), orderBook.bestAsk());
    }

    @Test
    void marketRemainderIsNotAddedToBook() {
        orderBook.submit(new Order(1, Side.SELL, 100, 30, 1000));

        List<Trade> trades = orderBook.submit(Order.market(2, Side.BUY, 50, 1001));

        assertEquals(List.of(new Trade(2, 1, 100, 30)), trades);
        assertTrue(orderBook.bestBid().isEmpty());
    }

    @Test
    void marketOrderOnEmptyBookProducesNoTrades() {
        orderBook.submit(Order.market(2, Side.BUY, 50, 1000));

        assertTrue(orderBook.isEmpty());
    }

    @Test
    void marketSellMatchesBestBid() {
        orderBook.submit(new Order(1, Side.BUY, 100, 50, 1000));

        List<Trade> trades = orderBook.submit(Order.market(2, Side.SELL, 50, 1001));

        assertEquals(List.of(new Trade(1, 2, 100, 50)), trades);
        assertTrue(orderBook.isEmpty());
    }

    @Test
    void rejectsDuplicateIdBeforeMatching() {
        orderBook.submit(new Order(1, Side.SELL, 100, 50, 1000));

        assertThrows(IllegalArgumentException.class, () -> orderBook.submit(new Order(1, Side.BUY, 100, 50, 1001)));
        assertEquals(OptionalLong.of(100), orderBook.bestAsk());
    }
}