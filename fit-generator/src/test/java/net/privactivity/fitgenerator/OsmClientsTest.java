package net.privactivity.fitgenerator;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OsmClientsTest {

    private static class StubHttpClient extends CachingHttpClient {
        private final String body;

        StubHttpClient(String body) {
            super(null, 0);
            this.body = body;
        }

        @Override
        public String get(String url) {
            return body;
        }
    }

    private String entry(String name, double lat, double lon) {
        return "{\"display_name\":\"%s\",\"lat\":\"%s\",\"lon\":\"%s\"}".formatted(name, lat, lon);
    }

    @Test
    void mergeDuplicateGeocoderHitsOfTheSameTown() {
        String body = "[" + entry("Utrecht, Nederland", 52.09, 5.12) + "," + entry("Utrecht, Nederland", 52.08, 5.13) + "]";
        NominatimGeocoder testee = new NominatimGeocoder(new StubHttpClient(body));

        Place place = testee.geocode("Utrecht");

        assertThat(place.displayName()).isEqualTo("Utrecht, Nederland");
        assertThat(place.location().lat()).isEqualTo(52.09);
    }

    @Test
    void listCandidatesForAmbiguousTown() {
        String body = "[" + entry("Frankfurt am Main, Hessen, Deutschland", 50.1, 8.6) + ","
                      + entry("Frankfurt (Oder), Brandenburg, Deutschland", 52.3, 14.5) + "]";
        NominatimGeocoder testee = new NominatimGeocoder(new StubHttpClient(body));

        assertThatThrownBy(() -> testee.geocode("Frankfurt"))
                .isInstanceOfSatisfying(AmbiguousPlaceException.class, e -> assertThat(e.candidates()).containsExactly(
                        "Frankfurt am Main, Hessen, Deutschland", "Frankfurt (Oder), Brandenburg, Deutschland"))
                .hasMessageContaining("Frankfurt (Oder), Brandenburg, Deutschland");
    }

    @Test
    void failForUnknownTown() {
        NominatimGeocoder testee = new NominatimGeocoder(new StubHttpClient("[]"));

        assertThatThrownBy(() -> testee.geocode("Nowhereville")).isInstanceOf(GenerationException.class)
                                                                .hasMessageContaining("No place found");
    }

    @Test
    void decodePolyline6Route() {
        String geometry = encode(List.of(new GeoPoint(38.5, -120.2), new GeoPoint(40.7, -120.95),
                                         new GeoPoint(43.252, -126.453)));
        String body = "{\"code\":\"Ok\",\"routes\":[{\"geometry\":\"" + geometry.replace("\\", "\\\\") + "\"}]}";
        OsrmRouteProvider testee = new OsrmRouteProvider(new StubHttpClient(body));

        List<GeoPoint> route = testee.route(new GeoPoint(0, 0), new GeoPoint(1, 1));

        assertThat(route).containsExactly(new GeoPoint(38.5, -120.2), new GeoPoint(40.7, -120.95),
                                          new GeoPoint(43.252, -126.453));
    }

    @Test
    void readElevationsInBatchesOf100() {
        StringBuilder values = new StringBuilder();
        List<GeoPoint> points = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            values.append(i == 0 ? "" : ",").append(i + 0.0);
            points.add(new GeoPoint(48, 11));
        }
        OpenMeteoElevationProvider testee = new OpenMeteoElevationProvider(
                new StubHttpClient("{\"elevation\":[" + values + "]}"));

        List<Double> elevations = testee.elevations(points);

        assertThat(elevations).hasSize(100).startsWith(0.0, 1.0).endsWith(99.0);
    }

    private String encode(List<GeoPoint> points) {
        StringBuilder sb = new StringBuilder();
        long prevLat = 0;
        long prevLon = 0;
        for (GeoPoint p : points) {
            long lat = Math.round(p.lat() * 1e6);
            long lon = Math.round(p.lon() * 1e6);
            encodeValue(lat - prevLat, sb);
            encodeValue(lon - prevLon, sb);
            prevLat = lat;
            prevLon = lon;
        }
        return sb.toString();
    }

    private void encodeValue(long value, StringBuilder sb) {
        long v = value < 0 ? ~(value << 1) : value << 1;
        while (v >= 0x20) {
            sb.append((char) ((0x20 | (v & 0x1f)) + 63));
            v >>= 5;
        }
        sb.append((char) (v + 63));
    }
}
