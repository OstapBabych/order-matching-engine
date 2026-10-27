package com.babych.matching;

public enum Side {
    BUY,
    SELL;

    public Side opposite() {
        return switch (this) {
            case BUY -> SELL;
            case SELL -> BUY;
        };
    }
}
