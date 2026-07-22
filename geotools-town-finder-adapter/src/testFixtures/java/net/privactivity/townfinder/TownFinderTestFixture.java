package net.privactivity.townfinder;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.fit.decode.FitDecoderImpl;
import net.privactivity.store.usecase.importactivities.adapter.FitDecoder;
import org.locationtech.jts.geom.Coordinate;
import java.io.IOException;
import java.io.InputStream;

public class TownFinderTestFixture {

    public static final Coordinate KASSEL_COORD = new Coordinate(51.3, 9.4);
    public static final LatLon KASSEL_LATLON = new LatLon(KASSEL_COORD.x, KASSEL_COORD.y);
    public static final Coordinate BERN_COORD = new Coordinate(46.94, 7.44);
    public static final LatLon BERN_LATLON = new LatLon(BERN_COORD.x, BERN_COORD.y);
    public static final Waypoint BERN_WAYPOINT = new Waypoint(1, Maybe.some(BERN_LATLON), Maybe.none(), Maybe.none(),
                                                              Maybe.none(), Maybe.none(),
                                                              Maybe.none(), Maybe.some(10), 1);

    public static Activity activityFromKomoot() {
        FitDecoder fitDecoder = new FitDecoderImpl();
        try (InputStream in = TownFinderTestFixture.class.getClassLoader().getResourceAsStream("komoot.fit")) {
            return fitDecoder.extractFit(in, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
