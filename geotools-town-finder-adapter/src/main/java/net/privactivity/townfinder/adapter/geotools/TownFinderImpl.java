package net.privactivity.townfinder.adapter.geotools;

import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Some;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;

public class TownFinderImpl implements TownFinder {


    private final List<SingleCoordinateTownFinder> townFinders;

    public TownFinderImpl(List<SingleCoordinateTownFinder> townFinders) {
        this.townFinders = townFinders;
    }

    @Override
    public List<String> findNearbyTowns(List<Waypoint> waypoints) {
        return findNearbyTownsFromLatLons(waypoints.stream()
                                                   .map(Waypoint::latlon)
                                                   .filter(Maybe::isPresent)
                                                   .map(Maybe::orThrow)
                                                   .toList());
    }

    @Override
    public List<String> findNearbyTownsFromLatLons(List<LatLon> latLons) {
        List<Coordinate> coordinates = latLonsToCoordinates(latLons);
        List<String> townNames = new ArrayList<>();
        String last = null;
        for (Coordinate coord : coordinates) {
            for (SingleCoordinateTownFinder finder : townFinders) {
                if (finder.townAt(coord) instanceof Some<String>(String townName) && !townName.equals(last)) {
                    townNames.add(townName);
                    last = townName;
                    break;
                }
            }
        }

        return townNames;
    }

    private List<Coordinate> latLonsToCoordinates(List<LatLon> waypoints) {
        return waypoints.stream()
                        .map(latlon -> new Coordinate(latlon.lat(), latlon.lon()))
                        .toList();
    }
}
