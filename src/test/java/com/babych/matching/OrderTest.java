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


    @Test
    void partialFillReducesRemainingQuantity() {
        order.fill(30);

        assertEquals(70, order.getRemainingQuantity());
        assertFalse(order.isFilled());
    }

    @Test
    void fullFillMarksOrderAsFilled() {
        order.fill(100);

        assertEquals(0, order.getRemainingQuantity());
        assertTrue(order.isFilled());
    }

    @Test
    void multiplePartialFillsCompleteOrder() {
        order.fill(30);
        order.fill(70);

        assertEquals(0, order.getRemainingQuantity());
        assertTrue(order.isFilled());
    }

    @Test
    void rejectsZeroFill() {
        assertThrows(IllegalArgumentException.class, () -> order.fill(0));
    }

    @Test
    void rejectsNegativeFill() {
        assertThrows(IllegalArgumentException.class, () -> order.fill(-5));
    }

    @Test
    void rejectsOverfillAndKeepsRemainingQuantity() {
        assertThrows(IllegalArgumentException.class, () -> order.fill(110));
        assertEquals(100, order.getRemainingQuantity());
    }

    @Test
    void rejectsFillOnFilledOrder() {
        order.fill(100);

        assertThrows(IllegalStateException.class, () -> order.fill(30));
    }

    @Test
    void fillDoesNotChangeOriginalQuantity() {
        order.fill(30);

        assertEquals(100, order.getOriginalQuantity());
    }
}