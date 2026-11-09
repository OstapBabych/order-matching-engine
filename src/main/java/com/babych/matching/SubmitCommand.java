package com.babych.matching;

import java.util.*;
import java.util.concurrent.CompletableFuture;

record SubmitCommand(Order order, CompletableFuture<List<Trade>> result)  implements Command{
}
