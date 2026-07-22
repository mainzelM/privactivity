package net.privactivity.jmh;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.MapBasedActivityRepository;
import net.privactivity.store.usecase.maxpower.MaxPower;
import net.privactivity.store.usecase.maxpower.MaxPowerUseCase;
import net.privactivity.townfinder.TownFinderTestFixture;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class MaxPowersBenchmark {

    Activity activity;
    private MaxPowerUseCase testee;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        testee = new MaxPowerUseCase(new MapBasedActivityRepository(Map.of()));
        activity = TownFinderTestFixture.activityFromKomoot();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
    public MaxPower benchmarkMaxPower() {
        return testee.maxPowerOne(activity);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MaxPowersBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class, "churn=true")
                //.addProfiler(StackProfiler.class, "lines=10")
                //.addProfiler(JavaFlightRecorderProfiler.class)
                .build();

        new Runner(opt).run();
    }

}
