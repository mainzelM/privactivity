package net.privactivity.domain;

import org.junit.jupiter.api.Test;
import java.util.List;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundsTest {

    @Test
    void intersects_with_itself() {
        Bounds b1 = Bounds.of(1, 2, 5, 6);

        assertTrue(b1.intersects(b1));
    }

    @Test
    void intersects_with_all_included() {
        Bounds b1 = Bounds.of(1, 10, 5, 60);
        Bounds b2 = Bounds.of(2, 4, 10, 20);

        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    void intersects_with_partially_northern() {
        Bounds b1 = Bounds.of(1, 10, 5, 60);
        Bounds b2 = Bounds.of(5, 15, 5, 60);

        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    void not_intersects_with_northern() {
        Bounds b1 = Bounds.of(1, 10, 5, 60);
        Bounds b2 = Bounds.of(11, 15, 5, 60);

        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    void not_intersects_with_eastern() {
        Bounds b1 = Bounds.of(1, 10, 5, 60);
        Bounds b2 = Bounds.of(1, 10, 61, 200);

        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    void bounds_of_single_waypoint() {
        Waypoint wp = waypointOf(new LatLon(43.0d, 8.0d));
        Bounds b = Bounds.ofWaypoints(List.of(wp));

        assertEquals(43.0d, b.south());
        assertEquals(43.0d, b.north());
        assertEquals(8.0d, b.west());
        assertEquals(8.0d, b.east());
    }

    @Test
    void bounds_of_two_waypoint_southern_hemisphere() {
        Waypoint wp1 = waypointOf(new LatLon(-43.0d, 8.0d));
        Waypoint wp2 = waypointOf(new LatLon(60.0d, -3.0d));
        Bounds b = Bounds.ofWaypoints(List.of(wp1, wp2));

        assertEquals(-43.0d, b.south());
        assertEquals(60.0d, b.north());
        assertEquals(-3.0d, b.west());
        assertEquals(8.0d, b.east());
    }

    @Test
    void bounds_of_two_waypoint_all_north_east() {
        Waypoint wp1 = waypointOf(new LatLon(43.0d, 8.0d));
        Waypoint wp2 = waypointOf(new LatLon(60.0d, 3.0d));
        Bounds b = Bounds.ofWaypoints(List.of(wp1, wp2));

        assertEquals(43.0d, b.south());
        assertEquals(60.0d, b.north());
        assertEquals(3.0d, b.west());
        assertEquals(8.0d, b.east());
    }

    private Waypoint waypointOf(LatLon latLon) {
        return new Waypoint(0, Maybe.some(latLon), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                            Maybe.none(), Maybe.none(), 1);
    }
}