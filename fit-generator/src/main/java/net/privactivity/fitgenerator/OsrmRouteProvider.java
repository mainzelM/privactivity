package net.privactivity.fitgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cycling routes from the community OSRM bike server of openstreetmap.de (no API key needed).
 */
public class OsrmRouteProvider implements RouteProvider {

    private static final String URL = "https://routing.openstreetmap.de/routed-bike/route/v1/driving/";

    private final CachingHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OsrmRouteProvider(CachingHttpClient http) {
        this.http = http;
    }

    @Override
    public List<GeoPoint> route(GeoPoint start, GeoPoint end) {
        String url = String.format(Locale.ROOT, "%s%.6f,%.6f;%.6f,%.6f?overview=full&geometries=polyline6",
                                   URL, start.lon(), start.lat(), end.lon(), end.lat());
        try {
            JsonNode root = mapper.readTree(http.get(url));
            if (!"Ok".equals(root.path("code").asText())) {
                throw new GenerationException("No route found: " + root.path("code").asText() + " "
                                              + root.path("message").asText());
            }
            return decodePolyline6(root.get("routes").get(0).get("geometry").asText());
        } catch (IOException e) {
            throw new GenerationException("Cannot parse routing response", e);
        }
    }

    List<GeoPoint> decodePolyline6(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        int index = 0;
        long lat = 0;
        long lon = 0;
        while (index < encoded.length()) {
            int[] latResult = decodeValue(encoded, index);
            index = latResult[1];
            lat += latResult[0];
            int[] lonResult = decodeValue(encoded, index);
            index = lonResult[1];
            lon += lonResult[0];
            points.add(new GeoPoint(lat / 1e6, lon / 1e6));
        }
        return points;
    }

    private int[] decodeValue(String encoded, int startIndex) {
        int index = startIndex;
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);
        int value = (result & 1) != 0 ? ~(result >> 1) : result >> 1;
        return new int[]{value, index};
    }
}
