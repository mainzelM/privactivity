package net.privactivity.fitgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Elevations from the keyless Open-Meteo Elevation API (max. 100 coordinates per request).
 */
public class OpenMeteoElevationProvider implements ElevationProvider {

    private static final String URL = "https://api.open-meteo.com/v1/elevation?";
    private static final int BATCH_SIZE = 100;

    private final CachingHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenMeteoElevationProvider(CachingHttpClient http) {
        this.http = http;
    }

    @Override
    public List<Double> elevations(List<GeoPoint> points) {
        List<Double> result = new ArrayList<>(points.size());
        for (int from = 0; from < points.size(); from += BATCH_SIZE) {
            List<GeoPoint> batch = points.subList(from, Math.min(from + BATCH_SIZE, points.size()));
            result.addAll(fetchBatch(batch));
        }
        return result;
    }

    private List<Double> fetchBatch(List<GeoPoint> batch) {
        StringBuilder lats = new StringBuilder();
        StringBuilder lons = new StringBuilder();
        for (GeoPoint p : batch) {
            if (!lats.isEmpty()) {
                lats.append(',');
                lons.append(',');
            }
            lats.append(String.format(Locale.ROOT, "%.5f", p.lat()));
            lons.append(String.format(Locale.ROOT, "%.5f", p.lon()));
        }
        try {
            JsonNode elevation = mapper.readTree(http.get(URL + "latitude=" + lats + "&longitude=" + lons))
                                       .get("elevation");
            if (elevation == null || elevation.size() != batch.size()) {
                throw new GenerationException("Unexpected elevation response");
            }
            List<Double> values = new ArrayList<>(batch.size());
            elevation.forEach(n -> values.add(n.asDouble()));
            return values;
        } catch (IOException e) {
            throw new GenerationException("Cannot parse elevation response", e);
        }
    }
}
