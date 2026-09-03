package net.privactivity.store.passes;

import net.privactivity.store.service.MountainPassService;
import net.privactivity.store.usecase.allcrossedpasses.AllCrossedPasses;
import net.privactivity.store.usecase.crossedpasses.CrossedPasses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passes")
public class MountainPassResource {
    private final MountainPassService mountainPassService;

    public MountainPassResource(MountainPassService mountainPassService) {
        this.mountainPassService = mountainPassService;
    }

    @GetMapping("/{activityId}")
    public CrossedPassesDTO get(@PathVariable long activityId) {
        CrossedPasses crossedPasses = mountainPassService.crossedPasses(activityId);
        return CrossedPassesDTO.from(crossedPasses);
    }

    @GetMapping("/all")
    public AllCrossedPassesDTO getAll() {
        AllCrossedPasses allCrossedPasses = mountainPassService.allCrossedPasses();
        return AllCrossedPassesDTO.from(allCrossedPasses);
    }
}
