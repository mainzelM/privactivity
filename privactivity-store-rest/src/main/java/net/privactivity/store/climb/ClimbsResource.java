package net.privactivity.store.climb;

import net.privactivity.domain.Climb;
import net.privactivity.store.repo.climb.ClimbRepository;
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
    private final ClimbRepository climbRepository;

    public ClimbsResource(ClimbRepository climbRepository) {
        this.climbRepository = climbRepository;
    }

    @GetMapping("")
    public Stream<Climb> allClimbs() {
        return climbRepository.getAllClimbs().stream();
    }

    @PostMapping("")
    public void addClimb(@RequestBody Climb climb) {
        climbRepository.addClimb(climb);
    }

    @DeleteMapping
    public void removeAll() {
        climbRepository.removeAllClimbs();
    }
}
