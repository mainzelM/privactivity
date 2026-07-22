package net.privactivity.domain;

import org.junit.jupiter.api.Test;
import java.util.List;



import static org.assertj.core.api.Assertions.assertThat;

class ClimbTest {

    @Test
    void ofActivity() {
        LatLon ll1 = new LatLon(1, 1);
        LatLon ll2 = new LatLon(1, 2);
        LatLon ll3 = new LatLon(1, 3);
        LatLon ll4 = new LatLon(1, 4);
        Waypoint wp1 = new Waypoint(1, Maybe.some(ll1), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.some(10), 1);
        int wp2Start = 11;
        Waypoint wp2 = new Waypoint(2, Maybe.some(ll2), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.some(wp2Start), 1);
        int wp3Start = 12;
        Waypoint wp3 = new Waypoint(3, Maybe.some(ll3), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.some(wp3Start), 1);
        Waypoint wp4 = new Waypoint(4, Maybe.some(ll4), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.some(13), 1);
        Activity a = new Activity(1, "", null, List.of(wp1, wp2, wp3, wp4), null)
                .withClimbs(List.of(new ClimbPointer(wp2Start, wp3Start, 0, 0,
                                                     Bounds.ofLatLons(List.of(ll2, ll3)))));

        List<Climb> climbs = Climb.of(a);

        assertThat(climbs)
                .containsExactly(new Climb(List.of(ll2, ll3)));
    }
}