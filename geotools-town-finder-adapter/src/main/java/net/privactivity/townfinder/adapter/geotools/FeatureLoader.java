package net.privactivity.townfinder.adapter.geotools;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.referencing.FactoryException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.referencing.CRS;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;



import static org.geotools.data.shapefile.ShapefileDataStoreFactory.CACHE_MEMORY_MAPS;
import static org.geotools.data.shapefile.ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX;
import static org.geotools.data.shapefile.ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX;
import static org.geotools.data.shapefile.ShapefileDataStoreFactory.MEMORY_MAPPED;

public class FeatureLoader {

    /**
     * Don't use this method as accessing the shapefile from classpath is very slow
     */
    @Deprecated
    private static SimpleFeatureCollection loadShapefileFromClasspath(String classpathLocation) {
        try {
            URL shapefileUrl = FeatureLoader.class.getClassLoader().getResource(classpathLocation);
            if (shapefileUrl == null) {
                throw new IllegalArgumentException("Shapefile not found on classpath: " + classpathLocation);
            }
            Map<String, Object> params = Map.of(
                    "url", shapefileUrl,
                    CREATE_SPATIAL_INDEX.key, Boolean.TRUE,
                    ENABLE_SPATIAL_INDEX.key, Boolean.TRUE,
                    CACHE_MEMORY_MAPS.key, Boolean.TRUE,
                    MEMORY_MAPPED.key, Boolean.TRUE);
            DataStore dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null) {
                throw new IOException("Unable to create DataStore for shapefile: " + classpathLocation);
            }
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            // Reproject to EPSG:4326
            return new ReprojectingFeatureCollection(featureSource.getFeatures(), CRS.decode("EPSG:4326"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleFeatureCollection loadShapefile(File shapefile) {
        try {
            return loadShapefileIntern(shapefile);
        } catch (Exception e) {
            throw new RuntimeException("While loading " + shapefile.getAbsolutePath(), e);
        }
    }

    private static SimpleFeatureCollection loadShapefileIntern(File shapefile) throws IOException,
            FactoryException {
        Map<String, URL> params = Map.of("url", shapefile.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        // Reproject to EPSG:4326
        return new ReprojectingFeatureCollection(featureSource.getFeatures(), CRS.decode("EPSG:4326"));
    }

}
