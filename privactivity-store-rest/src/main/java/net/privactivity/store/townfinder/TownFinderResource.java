package net.privactivity.store.townfinder;

import net.privactivity.store.service.TownFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/townfinder")
public class TownFinderResource {

    private final TownFinderService townFinderService;

    @Autowired
    public TownFinderResource(TownFinderService townFinderService) {
        this.townFinderService = townFinderService;
    }

    @GetMapping("/{activityId}")
    public List<String> findAllTowns(@PathVariable long activityId) {
        return townFinderService.findAllTowns(activityId);
    }

    @GetMapping("/{activityId}/bounds")
    public List<String> findBoundsTowns(@PathVariable long activityId) {
        return townFinderService.findBoundsTowns(activityId);
    }

    @GetMapping("/{activityId}/corners")
    public List<String> findCornerTowns(@PathVariable long activityId) {
        return townFinderService.findCornerTowns(activityId);
    }

    @PostMapping("/addMissing")
    public void addMissingTowns() {
        townFinderService.addMissingTowns();
    }

}
