package com.babych.matching.bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(OrderBookDepthBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
