package com.babych.matching;

import java.util.*;
import java.util.concurrent.CompletableFuture;

record Request(Order order, CompletableFuture<List<Trade>> result) {
}
