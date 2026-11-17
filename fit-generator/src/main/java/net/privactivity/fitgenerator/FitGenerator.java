package net.privactivity.fitgenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FitGenerator {

    static final double MAX_ROUTE_METERS = 200_000;
    // must match the file name format expected by ImportActivitiesUseCase
    static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH-mm-ss");
    private static final double ELEVATION_SPACING_METERS = 100;

    private final Geocoder geocoder;
    private final RouteProvider routeProvider;
    private final ElevationProvider elevationProvider;
    private final RiderProfile rider;
    private final ZoneId zone;

    public FitGenerator(Geocoder geocoder, RouteProvider routeProvider, ElevationProvider elevationProvider,
                        RiderProfile rider, ZoneId zone) {
        this.geocoder = geocoder;
        this.routeProvider = routeProvider;
        this.elevationProvider = elevationProvider;
        this.rider = rider;
        this.zone = zone;
    }

    public Path generate(GenerationRequest request) {
        Place from = geocoder.geocode(request.fromTown());
        Place to = geocoder.geocode(request.toTown());
        RouteGeometry route = new RouteGeometry(routeProvider.route(from.location(), to.location()));
        if (route.lengthMeters() > MAX_ROUTE_METERS) {
            throw new GenerationException(String.format("Route %s -> %s is %.0f km long, the maximum is %.0f km",
                                                        from.displayName(), to.displayName(),
                                                        route.lengthMeters() / 1000, MAX_ROUTE_METERS / 1000));
        }
        List<Double> elevations = elevationProvider.elevations(route.samplePoints(ELEVATION_SPACING_METERS));
        ElevationProfile profile = new ElevationProfile(ELEVATION_SPACING_METERS, route.lengthMeters(),
                                                        elevations);

        List<RideRecord> records = new RideSimulator(rider, request.seed()).simulate(route, profile);
        SessionSummary summary = SessionSummary.of(records, rider);

        ZonedDateTime localStart = ZonedDateTime.of(request.date(), request.time(), zone);
        Instant start = localStart.toInstant();
        Path file = request.outDir().resolve(localStart.format(FILE_NAME_FORMAT) + ".fit");
        try {
            Files.createDirectories(request.outDir());
        } catch (IOException e) {
            throw new GenerationException("Cannot create output directory " + request.outDir(), e);
        }
        new FitWriter(zone).write(file, start, records, summary, rider);
        return file;
    }
}
