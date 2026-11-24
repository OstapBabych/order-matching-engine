# Order Matching Engine

An order matching engine in Java 21 — the core part of a stock exchange.
It matches buy and sell orders using **price-time priority** and compares two ways
of making it thread-safe, measured with **JMH** benchmarks.

## Features

- Limit and market orders (market orders never rest in the book)
- **Price-time priority:** best price first; at the same price, the earlier order first
- Partial fills and multi-level sweeps (one order can fill against several prices)
- Trades happen at the price of the order already in the book
- Cancel an order by id
- Invalid orders and duplicate ids are rejected
- Thread-safe **single-writer** engine: many client threads, one matcher thread

## How it works

```
clients ──► MatchingEngine ──► queue ──► matcher thread ──► OrderBook
   ▲                                                            │
   └─────────────────── result (CompletableFuture) ◄────────────┘
```

The `OrderBook` keeps:

- **bids** and **asks** — `TreeMap<price, ArrayDeque<Order>>`, sorted so the best price is always first
- an **index** — `HashMap<orderId, Order>`, to find any order by id quickly

```java
MatchingEngine engine = new MatchingEngine(10_000);
engine.start();

engine.submit(new Order(1, Side.SELL, 10_125, 100, 1)).get();                   // rests in the book
List<Trade> trades = engine.submit(new Order(2, Side.BUY, 10_130, 40, 2)).get();
// [Trade[buyOrderId=2, sellOrderId=1, price=10125, quantity=40]]

engine.cancel(1).get();   // true
engine.stop();
```

## Design decisions

- **Prices are `long`, not `double`.** `double` has rounding errors (`0.1 + 0.2 != 0.3`).
  Prices are stored as whole ticks: `10125` = 101.25.
- **`TreeMap` for prices, `ArrayDeque` for orders at one price.** `TreeMap` gives the best
  price in O(log P). `ArrayDeque` is a fast FIFO queue, so the oldest order fills first.
- **`HashMap` index for cancel.** Finds an order by id in O(1) instead of searching the whole book.
- **`Trade` is an immutable `record`** that stores order ids, not `Order` objects,
  because orders keep changing after the trade.
- **Validate, then change.** All checks run before any state changes, so a failed request
  never leaves the book half-updated. This fixed a real bug where an order with a
  duplicate id could trade before the duplicate check ran.
- **A market order with a price can't be created.** `Order.market()` doesn't take a price,
  and the constructor that does is private.
- **Single writer.** Only one thread ever changes the book, so the matching code needs no
  locks and always gives the same result for the same input. Commands go through a
  `BlockingQueue` and are a `sealed interface`, handled with a pattern-matching `switch`.
- **Safe shutdown.** `stop()` finishes the current order, stops the matcher thread and
  fails all orders still waiting in the queue. I also found and fixed a
  **check-then-act race**: a client could pass the "is it stopped?" check just before
  `stop()` and leave its order stuck in the queue forever.

## Complexity

P = number of price levels, K = number of orders at one price.

| Operation | Complexity |
|---|---|
| Add an order | O(log P) |
| Best bid / ask | O(log P) |
| Match (per fill) | O(log P) |
| Cancel | O(1) to find + O(K) to remove |

## Benchmarks

Apple M5 (10 cores), 24 GB RAM, JDK 21. JMH, 3 forks, 3 warmup + 5 measurement iterations.
Raw results: [`bench-results/`](bench-results/).

**One thread, `OrderBook` only**

| Benchmark | Book depth | Orders/s | ≈ ns per order |
|---|---|---|---|
| Submit (buy/sell in turns) | – | 42 M | 24 |
| Add + cancel | 10 / 100 / 1000 levels | 37 M / 30 M / 24 M | 27 / 33 / 42 |
| Match | 10 / 100 / 1000 levels | 81 M / 78 M / 53 M | 12 / 13 / 19 |

**Many threads: `synchronized` book vs single-writer engine**

| Threads | `synchronized` (orders/s) | Single-writer (orders/s) |
|---|---|---|
| 1 | 41.5 M | 0.23 M |
| 2 | 4.1 M | 0.31 M |
| 4 | 3.5 M | 0.47 M |
| 8 | 3.8 M | 0.62 M |

**Latency with 4 threads**

| | `synchronized` | Single-writer |
|---|---|---|
| p50 (typical order) | 0.6 µs | 8 µs |
| p99.9 (1 in 1000 orders) | **427 µs** | **39 µs** |

### What the results show

- **With one thread the book is very fast:** about 24 ns per order.
- **`synchronized` gets ~10× slower with 2+ threads,** because threads wait for each other on one lock.
- **The single-writer engine is slower on average:** each order takes ~4 µs, but only
  ~24 ns of that is matching. The rest is passing the order between threads
  (the matcher and the client sleep and get woken up).
- **But it is ~11× better in the worst cases (p99.9).** The `synchronized` lock is not
  fair, so some threads wait much longer than others. The queue handles orders in order.
- Real exchanges keep the single-writer design but avoid the sleeping, using
  **busy-spin** and lock-free queues like the **LMAX Disruptor**.

## Limitations and next steps

- Busy-spin matcher, so threads don't sleep (next step)
- Lock-free queue (LMAX Disruptor style)
- Cancel is O(K) inside a price level
- No price limits for market orders
- One instrument, in memory only

## Build and test

Requires JDK 21+ and Maven.

```bash
mvn test   # 77 tests
```

Benchmarks: run `BenchmarkRunner` or `LatencyRunner` in `src/test/java/com/babych/matching/bench/`.
