package com.babych.matching;

import org.junit.jupiter.api.Test;

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
        orderBook.add(order11);

        assertEquals(OptionalLong.of(10), orderBook.bestBid());
    }

    @Test
    void addingSellOrderSetsBestAsk() {
        orderBook.add(order21);

        assertEquals(OptionalLong.of(10), orderBook.bestAsk());
    }

    @Test
    void bestBidIsHighestBuyPrice(){
        orderBook.add(order11);
        orderBook.add(order12);
        orderBook.add(order13);

        assertEquals(OptionalLong.of(15), orderBook.bestBid());
    }

    @Test
    void bestAskIsLowestSellPrice(){
        orderBook.add(order21);
        orderBook.add(order22);
        orderBook.add(order23);

        assertEquals(OptionalLong.of(5), orderBook.bestAsk());
    }

    @Test
    void buyAndSellAreStoredSeparately(){
        orderBook.add(order11);
        orderBook.add(order22);

        assertEquals(OptionalLong.of(10), orderBook.bestBid());
        assertEquals(OptionalLong.of(15), orderBook.bestAsk());
    }

    @Test
    void rejectsNullOrder(){
        assertThrows(NullPointerException.class, () -> orderBook.add(null));
    }
}