package net.privactivity.store.tiles;

import net.privactivity.store.service.TilesService;
import net.privactivity.store.usecase.gettiles.TileBounds;
import net.privactivity.store.usecase.gettiles.TileMetric;
import net.privactivity.store.usecase.gettiles.TilesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tiles")
public class TilesResource {

    private final TilesService tilesService;

    public TilesResource(TilesService tilesService) {
        this.tilesService = tilesService;
    }

    @GetMapping("")
    public TilesResponseDTO getTiles(@RequestParam(value = "metric", defaultValue = "activityCount") String metric,
                                     @RequestParam(value = "south", required = false) Double south,
                                     @RequestParam(value = "north", required = false) Double north,
                                     @RequestParam(value = "west", required = false) Double west,
                                     @RequestParam(value = "east", required = false) Double east) {
        TilesResponse response = tilesService.getTiles(TileMetric.fromParameter(metric), boundsOf(south, north, west,
                                                                                                  east));
        return new TilesResponseDTO(response);
    }

    private TileBounds boundsOf(Double south, Double north, Double west, Double east) {
        if (south == null || north == null || west == null || east == null) {
            return null;
        }
        return new TileBounds(south, north, west, east);
    }
}