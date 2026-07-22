package net.privactivity.jmh;

import net.privactivity.domain.Activity;
import net.privactivity.store.usecase.townfinder.findmaintowns.FindActivityMainTownsUseCase;
import net.privactivity.townfinder.TownFinderTestFixture;
import net.privactivity.townfinder.adapter.geotools.StandardTownFinders;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.DTraceAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class TownFinderBenchmark {
    private FindActivityMainTownsUseCase testee;

    //@org.openjdk.jmh.annotations.Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void blackHole(Blackhole blackhole) {
        blackhole.consume(new Object());
    }


    Activity activity;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        testee = new FindActivityMainTownsUseCase(StandardTownFinders.combinedTownFinder());
        activity = TownFinderTestFixture.activityFromKomoot();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 2)
    @Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
    public List<String> benchmarkFindsTownsOfFitFile() {
        return testee.act(activity);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include("benchmarkFindsTownsOfFitFile")
                //.addProfiler(GCProfiler.class, "churn=true")
                //.addProfiler(StackProfiler.class, "lines=10")
                //.addProfiler(JavaFlightRecorderProfiler.class)
                .addProfiler(DTraceAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }
/* No Profilers:
Benchmark                                         Mode  Cnt    Score   Error  Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile  avgt    6  105.045 ± 3.849  ms/op
 */
    /* GCProfiler only:
    Benchmark                                                                         Mode  Cnt         Score
    Error   Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile                                  avgt    6       104.379 ±      2
.988   ms/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.alloc.rate                    avgt    6       401.748 ±     11
.329  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.alloc.rate.norm               avgt    6  43968396.894 ±  48647
.210    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Eden_Space           avgt    6       401.874 ±     10
.327  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Eden_Space.norm      avgt    6  43982848.865 ± 515540
.001    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Survivor_Space       avgt    6         0.081 ±      0
.027  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Survivor_Space.norm  avgt    6      8860.186 ±   2943
.806    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.count                         avgt    6       418.000
   counts
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.time                          avgt    6       420.000
       ms
     */

    /* StackProfiler only, sampling rate default (10ms), "lines=10"
    Benchmark                                               Mode  Cnt    Score   Error  Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile        avgt    6  107.698 ± 4.393  ms/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:stack  avgt           NaN            ---
     */

    /* YourKit only, sampling_period_ms=20
    Benchmark                                         Mode  Cnt    Score   Error  Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile  avgt    6  121.959 ± 4.664  ms/op
     */

    /* JFR configName=default
    Benchmark                                             Mode  Cnt    Score    Error  Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile      avgt    6  121.507 ± 19.446  ms/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:jfr  avgt           NaN             ---
     */

    /* JFR configName=profile
    Benchmark                                             Mode  Cnt    Score    Error  Units
TownFinderBenchmark.benchmarkFindsTownsOfFitFile      avgt    6  119.731 ± 20.783  ms/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:jfr  avgt           NaN             ---
     */

    /* Using GCProfiler and StackProfiler:
    TownFinderBenchmark.benchmarkFindsTownsOfFitFile                                  avgt    6       112.465 ±
    24.257   ms/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.alloc.rate                    avgt    6       375.936 ±      77
.004  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.alloc.rate.norm               avgt    6  44127973.504 ±   92277
.259    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Eden_Space           avgt    6       377.001 ±      81
.934  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Eden_Space.norm      avgt    6  44241537.365 ± 1067027
.447    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Survivor_Space       avgt    6         0.083 ±       0
.039  MB/sec
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.churn.G1_Survivor_Space.norm  avgt    6      9839.673 ±    6167
.139    B/op
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.count                         avgt    6       413.000
    counts
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:gc.time                          avgt    6       484.000
        ms
TownFinderBenchmark.benchmarkFindsTownsOfFitFile:stack                            avgt                NaN
       ---
     *
     */
}
