package net.privactivity.fitgenerator;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class FitGeneratorMain {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final String USAGE = """
            Usage: --from "<town>" --to "<town>" [--date yyyy-MM-dd] [--time HH:mm] [--seed N] [--out-dir DIR]
              --date     local date (Europe/Berlin), default: today
              --time     local start time (Europe/Berlin), default: 08:00
              --seed     random seed for reproducible rides, default: 1
              --out-dir  output directory, default: directory the tool was started from
            """;

    static void main(String[] args) {
        System.exit(new FitGeneratorMain().run(args));
    }

    int run(String[] args) {
        try {
            Map<String, String> options = parseOptions(args);
            GenerationRequest request = toRequest(options);
            Path cacheDir = Path.of(System.getProperty("user.home"), ".privactivity-fit-generator", "cache");
            FitGenerator generator = new FitGenerator(
                    new NominatimGeocoder(new CachingHttpClient(cacheDir.resolve("nominatim"), 1100)),
                    new OsrmRouteProvider(new CachingHttpClient(cacheDir.resolve("osrm"), 1000)),
                    new OpenMeteoElevationProvider(new CachingHttpClient(cacheDir.resolve("elevation"), 1000)),
                    new RiderProfile(), ZONE);
            Path file = generator.generate(request);
            System.out.println("Created " + file.toAbsolutePath());
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            return 2;
        } catch (GenerationException e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

    private Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!args[i].startsWith("--") || i + 1 >= args.length) {
                throw new IllegalArgumentException("Invalid argument: " + args[i]);
            }
            options.put(args[i].substring(2), args[i + 1]);
        }
        return options;
    }

    private GenerationRequest toRequest(Map<String, String> options) {
        for (String key : options.keySet()) {
            if (!java.util.List.of("from", "to", "date", "time", "seed", "out-dir").contains(key)) {
                throw new IllegalArgumentException("Unknown option --" + key);
            }
        }
        String from = options.get("from");
        String to = options.get("to");
        if (from == null || to == null) {
            throw new IllegalArgumentException("--from and --to are required");
        }
        try {
            LocalDate date = options.containsKey("date") ? LocalDate.parse(options.get("date"))
                                                        : LocalDate.now(ZONE);
            LocalTime time = options.containsKey("time") ? LocalTime.parse(options.get("time"))
                                                        : LocalTime.of(8, 0);
            long seed = options.containsKey("seed") ? Long.parseLong(options.get("seed")) : 1;
            Path outDir = Path.of(options.getOrDefault("out-dir", System.getProperty(
                    "fitgenerator.invocationDir", System.getProperty("user.dir"))));
            return new GenerationRequest(from, to, date, time, seed, outDir);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid option value: " + e.getMessage());
        }
    }
}
