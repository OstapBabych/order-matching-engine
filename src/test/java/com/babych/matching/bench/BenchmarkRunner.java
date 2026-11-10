package com.babych.matching.bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        for (int threads : new int[]{1, 2, 4, 8}) {
            Options options = new OptionsBuilder()
                    .include(ConcurrencyBenchmark.class.getSimpleName())
                    .threads(threads)
                    .build();
            new Runner(options).run();
        }
    }
}
