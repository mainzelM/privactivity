package net.privactivity.townfinder.adapter.geotools;

import net.privactivity.domain.Maybe;
import org.locationtech.jts.geom.Coordinate;

public interface SingleCoordinateTownFinder {
    Maybe<String> townAt(Coordinate coord);
}
