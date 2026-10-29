package com.babych.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeTest {

    Trade trade = new Trade(1, 2, 10, 100);

    @Test
    void gettersReturnCorrectValue() {
        assertEquals(1, trade.buyOrderId());
        assertEquals(2, trade.sellOrderId());
        assertEquals(10, trade.price());
        assertEquals(100, trade.quantity());
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Trade(1, 2, 0, 100));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Trade(1, 2, -10, 100));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Trade(1, 2, 10, 0));
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Trade(1, 2, 10, -100));
    }
}