package com.babych.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class MatchingEngine {
    private final OrderBook book;
    private final BlockingQueue<Request> queue;
    private Thread matcherThread;
    private volatile boolean stopped;

    public MatchingEngine(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, got: " + capacity);
        }
        this.book = new OrderBook();
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public CompletableFuture<List<Trade>> submit(Order order) throws InterruptedException {
        Objects.requireNonNull(order, "order must not be null");

        if (stopped) {
            throw new IllegalStateException("engine stopped");
        }

        CompletableFuture<List<Trade>> future = new CompletableFuture<>();
        queue.put(new Request(order, future));
        return future;
    }

    private void runLoop() {
        try {
            while (true) {
                Request request = queue.take();
                try {
                    List<Trade> trades = book.submit(request.order());
                    request.result().complete(trades);
                } catch (RuntimeException e) {
                    request.result().completeExceptionally(e);
                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    public void start() {
        if (matcherThread != null) {
            throw new IllegalStateException("engine already started");
        }
        matcherThread = new Thread(this::runLoop, "matcher");
        matcherThread.start();
    }

    public void stop() throws InterruptedException {
        stopped = true;

        if (matcherThread != null) {
            matcherThread.interrupt();
            matcherThread.join();
        }

        List<Request> pending = new ArrayList<>();
        queue.drainTo(pending);
        for (Request request : pending) {
            request.result().completeExceptionally(new IllegalStateException("engine stopped"));
        }
    }

    public boolean isRunning() {
        return matcherThread != null && matcherThread.isAlive();
    }
}
