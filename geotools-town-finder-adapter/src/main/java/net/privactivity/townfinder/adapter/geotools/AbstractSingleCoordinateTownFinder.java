package net.privactivity.townfinder.adapter.geotools;

import net.privactivity.domain.Maybe;
import net.privactivity.domain.None;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

abstract class AbstractSingleCoordinateTownFinder implements SingleCoordinateTownFinder {

    private final SimpleFeatureCollection featureCollection;
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private final FilterFactory ff;

    AbstractSingleCoordinateTownFinder(SimpleFeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        this.ff = CommonFactoryFinder.getFilterFactory();
    }

    @Override
    public Maybe<String> townAt(Coordinate coord) {
        Point point = geometryFactory.createPoint(coord);

        // distance
        // units
        Filter filter = ff.dwithin(
                ff.property("the_geom"),
                ff.literal(point),
                0,                        // distance
                "ignored"               // units
                                  );

        SimpleFeatureCollection results = featureCollection.subCollection(filter);

        try (SimpleFeatureIterator iterator = results.features()) {
            if (!iterator.hasNext()) {
                return new None<>();
            }
            SimpleFeature feature = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("Found more than one feature for " + coord);
            }
            return extractTownName(feature);
        }
    }

    protected abstract Maybe<String> extractTownName(SimpleFeature feature);

}
