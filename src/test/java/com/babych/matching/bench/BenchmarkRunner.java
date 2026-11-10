package com.babych.matching.bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        Options singleThread = new OptionsBuilder()
                .include("OrderBookBenchmark|OrderBookDepthBenchmark")
                .forks(3)
                .output("bench-results/single-thread-throughput.txt")
                .build();
        new Runner(singleThread).run();

        for (int threads : new int[]{1, 2, 4, 8}) {
            Options options = new OptionsBuilder()
                    .include(ConcurrencyBenchmark.class.getSimpleName())
                    .threads(threads)
                    .forks(3)
                    .output("bench-results/concurrency-throughput-" + threads + "-threads.txt")
                    .build();
            new Runner(options).run();
        }
    }
}
