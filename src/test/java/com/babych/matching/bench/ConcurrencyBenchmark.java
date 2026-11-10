package com.babych.matching.bench;

import com.babych.matching.*;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)

public class ConcurrencyBenchmark {

    @State(Scope.Benchmark)
    public static class SyncState {
        OrderBook book;
        AtomicLong nextId;

        @Setup(Level.Iteration)
        public void setUp() {
            book = new OrderBook();
            nextId = new AtomicLong();
        }
    }

    @State(Scope.Benchmark)
    public static class EngineState {
        MatchingEngine engine;
        AtomicLong nextId;

        @Setup(Level.Trial)
        public void setUp() {
            engine = new MatchingEngine(1024);
            engine.start();
            nextId = new AtomicLong();
        }

        @TearDown(Level.Trial)
        public void tearDown() throws InterruptedException {
            engine.stop();
        }
    }

    @Benchmark
    public List<Trade> synchronizedBook(SyncState state) {
        long id = state.nextId.incrementAndGet();
        Side side = (id % 2 == 0) ? Side.BUY : Side.SELL;
        return state.book.submit(new Order(id, side, 100, 1, 0));
    }

    @Benchmark
    public List<Trade> singleWriterEngine(EngineState state) throws Exception {
        long id = state.nextId.incrementAndGet();
        Side side = (id % 2 == 0) ? Side.BUY : Side.SELL;
        return state.engine.submit(new Order(id, side, 100, 1, 0)).get();
    }
}
