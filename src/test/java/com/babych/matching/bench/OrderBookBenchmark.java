package com.babych.matching.bench;

import com.babych.matching.Order;
import com.babych.matching.OrderBook;
import com.babych.matching.Side;
import com.babych.matching.Trade;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)

public class OrderBookBenchmark {
    OrderBook book;
    long nextId;

    @Setup(Level.Iteration)
    public void setUp() {
        book = new OrderBook();
        nextId = 1;
    }

    @Benchmark
    public List<Trade> submitAlternatingBuySell() {
        long id = nextId++;

        Side side = (id % 2 == 0) ? Side.BUY : Side.SELL;

        Order order = new Order(id, side, 100, 1, 1000);

        return book.submit(order);
    }
}
