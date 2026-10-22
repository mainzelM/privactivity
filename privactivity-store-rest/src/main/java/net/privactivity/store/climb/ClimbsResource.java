package net.privactivity.store.climb;

import net.privactivity.domain.Climb;
import net.privactivity.store.service.ClimbService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/climbs")
public class ClimbsResource {
    private final ClimbService climbService;

    public ClimbsResource(ClimbService climbService) {
        this.climbService = climbService;
    }

    @GetMapping("")
    public Stream<Climb> allClimbs() {
        return climbService.getAllClimbs().stream();
    }

    @PostMapping("")
    public void addClimb(@RequestBody Climb climb) {
        climbService.addClimb(climb);
    }

    @DeleteMapping
    public void removeAll() {
        climbService.removeAllClimbs();
    }
}
