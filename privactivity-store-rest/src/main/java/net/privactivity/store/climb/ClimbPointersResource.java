package net.privactivity.store.climb;

import net.privactivity.domain.ClimbPointer;
import net.privactivity.store.service.ClimbService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/climb-pointers")
public class ClimbPointersResource {
    private final ClimbService climbService;

    public ClimbPointersResource(ClimbService climbService) {
        this.climbService = climbService;
    }

    @GetMapping("")
    public Map<Long, Set<ClimbPointer>> allClimbs() {
        return climbService.getAllClimbPointers();
    }

    @GetMapping("/{activityId}")
    public Set<ClimbPointer> get(@PathVariable long activityId) {
        return climbService.climbsOf(activityId);
    }

    @PostMapping("/{activityId}")
    public void addClimbs(@PathVariable long activityId, @RequestBody Set<ClimbPointer> climbs) {
        climbService.addActivityClimbPointers(activityId, climbs);
    }

    @DeleteMapping
    public void removeAll() {
        climbService.removeAllClimbPointers();
    }
}
