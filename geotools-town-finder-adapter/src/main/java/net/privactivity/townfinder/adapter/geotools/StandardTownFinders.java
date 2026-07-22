package net.privactivity.townfinder.adapter.geotools;

import net.privactivity.domain.Maybe;
import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;

public class StandardTownFinders {
    private static final Logger logger = LoggerFactory.getLogger(StandardTownFinders.class);

    // TODO: is the static really worth it?
    private static SimpleFeatureCollection germanySFC;
    private static SimpleFeatureCollection swissSFC;

    public static TownFinder combinedTownFinder() {
        return new TownFinderImpl(List.of(swissTownFinder(), germanyTownFinder()));
    }

    static SingleCoordinateTownFinder swissTownFinder() {
        try {
            return new SwissTownFinder(swissFeatureCollection());
        } catch (Exception e) {
            logger.warn("Failed to create SwissTownFinder. Will use EmptyTownFinder", e);
            return new EmptyTownFinder();
        }
    }

    public static SimpleFeatureCollection swissFeatureCollection() {
        if (swissSFC == null) {
            File shapefile = ShapefileConfig.getSwissShapefilePath();
            if (!shapefile.exists()) {
                throw new RuntimeException("Swiss shapefile not found: " + shapefile.getAbsolutePath() +
                                           ". Please ensure shapefiles are moved to external data directory.");
            }
            swissSFC = FeatureLoader.loadShapefile(shapefile);
        }
        return swissSFC;
    }

    static SingleCoordinateTownFinder germanyTownFinder() {
        try {
            return new GermanyTownFinder(germanyFeatureCollection());
        } catch (Exception e) {
            logger.warn("Failed to create GermanyTownFinder. Will use EmptyTownFinder", e);
            return new EmptyTownFinder();
        }
    }

    private static SimpleFeatureCollection germanyFeatureCollection() {
        if (germanySFC == null) {
            File shapefile = ShapefileConfig.getGermanShapefilePath();
            if (!shapefile.exists()) {
                throw new RuntimeException("German shapefile not found: " + shapefile.getAbsolutePath() +
                                           ". Please ensure shapefiles are moved to external data directory.");
            }
            germanySFC = FeatureLoader.loadShapefile(shapefile);
        }
        return germanySFC;
    }

    private static class EmptyTownFinder implements SingleCoordinateTownFinder {

        @Override
        public Maybe<String> townAt(Coordinate coord) {
            return Maybe.none();
        }
    }
}
