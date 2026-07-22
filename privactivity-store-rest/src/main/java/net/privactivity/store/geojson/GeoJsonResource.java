package net.privactivity.store.geojson;

import net.privactivity.store.service.GeoJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("geojson")
public class GeoJsonResource {

    private final GeoJsonService geoJsonService;

    @Autowired
    public GeoJsonResource(GeoJsonService geoJsonService) {
        this.geoJsonService = geoJsonService;
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public Object activityAsGeoJson(
            @PathVariable("id") Long activityId,
            @RequestParam(name = "climbs", defaultValue = "false") Boolean includeClimbs) {
        return geoJsonService.asGeoJson(activityId, includeClimbs);
    }
}
