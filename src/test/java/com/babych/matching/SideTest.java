package com.babych.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SideTest {

    @Test
    void buyOppositeIsSell() {
        assertEquals(Side.SELL, Side.BUY.opposite());
    }

    @Test
    void sellOppositeIsBuy() {
        assertEquals(Side.BUY, Side.SELL.opposite());
    }
}