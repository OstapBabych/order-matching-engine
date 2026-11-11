# Order Matching Engine

A limit order book and matching engine written in Java 21. It implements
price-time priority matching with limit and market (IOC) orders, partial fills,
multi-level sweeps and order cancellation, and compares two concurrency designs —
a `synchronized` order book and a single-writer engine — using JMH benchmarks.

## Roadmap

- [x] Phase 1 — Core order book
  - `Order` / `Side` / `OrderType` / `Trade` domain model with fail-fast validation
  - `OrderBook` with price-time priority: limit and market orders, partial fills, multi-level sweeps
  - Order cancellation with O(1) id lookup, duplicate order id rejection
- [x] Phase 2 — Concurrency
  - Thread-safe `synchronized` `OrderBook`
  - Single-writer `MatchingEngine`: `BlockingQueue`, one matcher thread, `CompletableFuture` results, sealed command types
  - Graceful shutdown and concurrency tests
- [x] Phase 3 — Benchmarking
  - JMH throughput benchmarks: single-threaded, by book depth, multi-threaded
  - Latency percentiles (p50 / p99 / p99.9)
- [ ] Phase 4 — Documentation
  - Architecture, design decisions, complexity and benchmark results
- [ ] Next — busy-spin wait strategy for the single-writer engine

## Documentation

🚧 **In progress.** Architecture, design decisions and benchmark results are being
written up. Raw benchmark output is already available in [`bench-results/`](bench-results/).
