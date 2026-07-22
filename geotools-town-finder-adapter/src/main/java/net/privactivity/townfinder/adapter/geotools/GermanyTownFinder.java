package net.privactivity.townfinder.adapter.geotools;

import net.privactivity.domain.Maybe;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;

public class GermanyTownFinder extends AbstractSingleCoordinateTownFinder {
    GermanyTownFinder(SimpleFeatureCollection featureCollection) {
        super(featureCollection);
    }

    @Override
    protected Maybe<String> extractTownName(SimpleFeature feature) {
        return Maybe.nullAsNone(feature.getAttribute("GEN")).map(Object::toString);
    }
}
