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

public class OrderBookDepthBenchmark {

    private static final long BASE = 1000000;

    @Param({"10", "100", "1000"})
    public int levels;

    OrderBook book;
    long nextId;

    @Setup(Level.Iteration)
    public void setUp() {
        book = new OrderBook();
        nextId = 1;

        for (int level = 1; level <= levels; level++) {
            book.submit(new Order(nextId++, Side.BUY, BASE - level, 100, 0));

            long sellQuantity = (level == 1) ? 1000000000L : 100;
            book.submit(new Order(nextId++, Side.SELL, BASE + level, sellQuantity, 0));
        }
    }

    @Benchmark
    public boolean addAndCancel() {
        long id = nextId++;
        long price = BASE - 1 - (id % levels);

        book.submit(new Order(id, Side.BUY, price, 1, 1000));

        return book.cancel(id);
    }

    @Benchmark
    public List<Trade> matchAgainstDeepBook() {
        return book.submit(Order.market(nextId++, Side.BUY, 1, 0));
    }
}
