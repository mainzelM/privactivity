package net.privactivity.townfinder.adapter.geotools;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import java.util.List;

public final class GeoToolsUtil {
    private GeoToolsUtil() {
    }

    public static void printAllAttributes(SimpleFeatureCollection featureCollection1) {
        SimpleFeatureType schema = featureCollection1.getSchema();

        List<AttributeDescriptor> schemaAttributes = schema.getAttributeDescriptors();
        for (AttributeDescriptor attr : schemaAttributes) {
            String name = attr.getLocalName();
            String type = attr.getType().getBinding().getSimpleName();
            System.out.println(name + " : " + type);
        }

        System.out.println("Source CRS: " + featureCollection1.getSchema().getCoordinateReferenceSystem());

    }

    public static void printAllFeatures(SimpleFeatureCollection featureCollection) {
        printAllAttributes(featureCollection);

        int numFeatures = 0;
        try (SimpleFeatureIterator features = featureCollection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                numFeatures++;
                String name = (String) feature.getAttribute("NAME");
                if (name.contains("xyz")) {
                    System.out.println("  Name: " + name);

                    List<Object> attributes = feature.getAttributes();
                    for (Object attr : attributes) {
                        int idx = attributes.indexOf(attr);
                        String attrName = feature.getFeatureType().getDescriptor(idx).getLocalName();
                        System.out.println("  " + attrName + ": " + attr);
                    }
                }
            }
        }
        System.out.println("Total features: " + numFeatures);

    }
}
