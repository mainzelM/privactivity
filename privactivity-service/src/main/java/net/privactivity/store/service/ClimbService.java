package net.privactivity.store.service;

import net.privactivity.domain.Climb;
import net.privactivity.domain.ClimbPointer;
import net.privactivity.store.repo.climb.ClimbRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class ClimbService {

    private final ClimbRepository climbRepository;

    public ClimbService(ClimbRepository climbRepository) {
        this.climbRepository = climbRepository;
    }

    public Set<Climb> getAllClimbs() {
        return climbRepository.getAllClimbs();
    }

    public long addClimb(Climb climb) {
        return climbRepository.addClimb(climb);
    }

    public void removeAllClimbs() {
        climbRepository.removeAllClimbs();
    }

    public Map<Long, Set<ClimbPointer>> getAllClimbPointers() {
        return climbRepository.getAllClimbPointers();
    }

    public Set<ClimbPointer> climbsOf(long activityId) {
        return climbRepository.climbsOf(activityId);
    }

    public long addActivityClimbPointers(long activityId, Set<ClimbPointer> climbs) {
        return climbRepository.addActivityClimbPointers(activityId, climbs);
    }

    public void removeAllClimbPointers() {
        climbRepository.removeAllClimbPointers();
    }
}
