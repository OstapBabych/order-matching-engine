package com.babych.matching;

import java.util.concurrent.CompletableFuture;

sealed interface Command permits SubmitCommand, CancelCommand{
    CompletableFuture<?> result();
}
