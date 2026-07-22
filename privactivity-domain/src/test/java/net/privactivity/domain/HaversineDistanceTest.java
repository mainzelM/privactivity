package net.privactivity.domain;

import org.junit.jupiter.api.Test;



import static org.assertj.core.api.Assertions.assertThat;

class HaversineDistanceTest {

    @Test
    void calculateDistanceBetweenRioDeJaneiroAndSaoPaulo() {
        double dist = HaversineDistance.distanceInMeter(-22.906847, -43.172896, -23.550520, -46.633309);

        assertThat(dist / 1000).isBetween(357., 363.);
    }

    @Test
    void calculateDistanceBetweenHomeAndBorder() {
        double dist = HaversineDistance.distanceInMeter(46.510099, 7.378650, 46.513056, 7.370834);

        assertThat(dist).isBetween(500., 800.);
    }

    @Test
    void calculateDistanceBetweenHomeAndBorderForWaypoints() {
        Waypoint start = new Waypoint(0, Maybe.some(new LatLon(46.510099, 7.378650)), Maybe.none(), Maybe.none(),
                                      Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), 1);
        Waypoint end = new Waypoint(0, Maybe.some(new LatLon(46.513056, 7.370834)), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), 1);
        double dist = HaversineDistance.distanceInMeter(start, end).orThrow();

        assertThat(dist).isBetween(500., 800.);
    }
}