package com.babych.matching;

import java.util.concurrent.CompletableFuture;

record CancelCommand(long orderId, CompletableFuture<Boolean> result) implements Command {
}
