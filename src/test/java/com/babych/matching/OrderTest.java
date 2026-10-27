package com.babych.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    final long id = 1;
    final Side side = Side.BUY;
    final long price = 10;
    final long quantity = 100;
    final long timestamp = 1000;

    Order order = new Order(id, side, price, quantity, timestamp);

    @Test
    void validIdGetter() {
        assertEquals(id, order.getId());
    }

    @Test
    void validSideGetter() {
        assertEquals(side, order.getSide());
    }

    @Test
    void validPriceGetter() {
        assertEquals(price, order.getPrice());
    }

    @Test
    void validOriginalQuantityGetter() {
        assertEquals(quantity, order.getOriginalQuantity());
    }

    @Test
    void validRemainingGetter() {
        assertEquals(quantity, order.getRemainingQuantity());
    }

    @Test
    void validTimestampGetter() {
        assertEquals(timestamp, order.getTimestamp());
    }

    @Test
    void rejectsZeroId() {
        assertThrows(IllegalArgumentException.class, () -> new Order(0, Side.BUY, 10, 100, 1000));
    }

    @Test
    void rejectsNegativeId() {
        assertThrows(IllegalArgumentException.class, () -> new Order(-1, Side.BUY, 10, 100, 1000));
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, Side.BUY, 0, 100, 1000));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, Side.BUY, -1, 100, 1000));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, Side.BUY, 1, 0, 1000));
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, Side.BUY, 1, -1, 1000));
    }
    
    @Test
    void rejectsNullSide() {
        assertThrows(NullPointerException.class, () -> new Order(1, null, 10, 100, 1000));
    }
}