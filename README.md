# Order Matching Engine

A Java implementation of a stock/crypto exchange order-matching engine,
built as a CV project to demonstrate data structures, concurrency and
performance benchmarking.

## Roadmap

- [ ] Phase 1 — Core order book (single-threaded)
  - Order / Side / OrderType model classes
  - OrderBook with price-time priority
  - Basic matching logic (limit orders, partial fills)
- [ ] Phase 2 — Concurrency
  - Thread-safe order submission
  - Concurrent order processing
- [ ] Phase 3 — Benchmarking
  - Throughput (orders/sec) and latency (p50/p95/p99) measurements
- [ ] Phase 4 — Tests
  - JUnit 5 tests for matching edge cases (partial fills, cancellations,
    crossed book)
- [ ] Phase 5 — Polish
  - README with architecture + benchmark results, publish to GitHub

## Status

Just started — Phase 1 in progress.
