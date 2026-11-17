package net.privactivity.fitgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NominatimGeocoder implements Geocoder {

    private static final String URL = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5"
                                      + "&featuretype=settlement&q=";

    private final CachingHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public NominatimGeocoder(CachingHttpClient http) {
        this.http = http;
    }

    @Override
    public Place geocode(String townName) {
        String body = http.get(URL + URLEncoder.encode(townName, StandardCharsets.UTF_8));
        Map<String, Place> placesByName = new LinkedHashMap<>();
        try {
            for (JsonNode node : mapper.readTree(body)) {
                String name = node.get("display_name").asText();
                // the same town is often returned as several OSM objects (city, municipality, ...)
                placesByName.putIfAbsent(name, new Place(name, new GeoPoint(node.get("lat").asDouble(),
                                                                            node.get("lon").asDouble())));
            }
        } catch (IOException e) {
            throw new GenerationException("Cannot parse geocoder response for '" + townName + "'", e);
        }
        if (placesByName.isEmpty()) {
            throw new GenerationException("No place found for '" + townName + "'");
        }
        if (placesByName.size() > 1) {
            throw new AmbiguousPlaceException(townName, List.copyOf(placesByName.keySet()));
        }
        return placesByName.values().iterator().next();
    }
}
