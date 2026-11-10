package com.babych.matching.bench;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class LatencyRunner {
    public static void main(String[] args) throws Exception {
        for (int threads : new int[]{1, 4}) {
            Options options = new OptionsBuilder()
                    .mode(Mode.SampleTime)
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .include(ConcurrencyBenchmark.class.getSimpleName())
                    .threads(threads)
                    .forks(3)
                    .output("bench-results/latency-" + threads + "-threads.txt")
                    .build();

            new Runner(options).run();
        }
    }
}