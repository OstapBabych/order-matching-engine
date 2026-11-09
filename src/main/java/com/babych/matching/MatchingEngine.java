package com.babych.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class MatchingEngine {
    private final OrderBook book;
    private final BlockingQueue<Command> queue;
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
        SubmitCommand command = new SubmitCommand(order, future);
        queue.put(command);
        if (stopped && queue.remove(command)) {
            command.result().completeExceptionally(new IllegalStateException("engine stopped"));
        }
        return future;
    }

    public CompletableFuture<Boolean> cancel(long orderId) throws InterruptedException {
        if (stopped) {
            throw new IllegalStateException("engine stopped");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CancelCommand command = new CancelCommand(orderId, future);
        queue.put(command);
        if (stopped && queue.remove(command)) {
            command.result().completeExceptionally(new IllegalStateException("engine stopped"));
        }
        return future;
    }

    private void runLoop() {
        try {
            while (true) {
                Command command = queue.take();

                try {
                    switch (command) {
                        case SubmitCommand submit -> {
                            submit.result().complete(book.submit(submit.order()));
                        }
                        case CancelCommand cancel -> {
                            cancel.result().complete(book.cancel(cancel.orderId()));
                        }
                    }
                } catch (RuntimeException e) {
                    command.result().completeExceptionally(e);
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

        List<Command> pending = new ArrayList<>();
        queue.drainTo(pending);
        for (Command command : pending) {
            command.result().completeExceptionally(new IllegalStateException("engine stopped"));
        }
    }

    public boolean isRunning() {
        return matcherThread != null && matcherThread.isAlive();
    }
}
