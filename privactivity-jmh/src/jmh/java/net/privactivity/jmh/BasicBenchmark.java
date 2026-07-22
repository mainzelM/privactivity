package net.privactivity.jmh;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

public class BasicBenchmark {

    @org.openjdk.jmh.annotations.Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    public void blackHole(Blackhole blackhole) {
        blackhole.consume(new Object());
    }

    @org.openjdk.jmh.annotations.Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    public void blackHole2(Blackhole blackhole) {
        blackhole.consume(new Object());
    }
}
