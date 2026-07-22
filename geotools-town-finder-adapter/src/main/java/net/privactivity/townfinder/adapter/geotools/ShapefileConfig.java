package net.privactivity.townfinder.adapter.geotools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ShapefileConfig {

    private static final Logger logger = LoggerFactory.getLogger(ShapefileConfig.class);

    // TODO: get from SpringBoot properties or something else
    public static String getDataDirectory() {
        String envDir = System.getenv("PRIVACTIVITY_SHAPEFILE_DATA_DIR");

        if (envDir != null) {
            logger.info("Using environment variable PRIVACTIVITY_SHAPEFILE_DATA_DIR: {}", envDir);
            return envDir;
        } else {
            logger.info("Environment variable PRIVACTIVITY_SHAPEFILE_DATA_DIR not set");
        }

        // TODO: currently not used
        String propDir = System.getProperty("privactivity.townfinder.shapefile-data-dir");
        if (propDir != null) {
            logger.info("Using system property privactivity.townfinder.shapefile-data-dir: {}", propDir);
            return propDir;
        } else {
            logger.info("System property privactivity.townfinder.shapefile-data-dir not set");
        }

        // In container environments, bootJar copies data dir to /, which maps to
        // /workspace in buildpack
        String containerPath = "/workspace/data/shapefiles";
        if (new File(containerPath).exists()) {
            logger.info("Using container path {}", containerPath);
            return containerPath;
        } else {
            logger.info("Container path {} does not exist", containerPath);
        }
        logger.info("Falling back to local development path privactivty-app/data/shapefiles");
        // Fall back to local development path
        return "privactivty-app/data/shapefiles";
    }

    /**
     * Get the absolute path to the Swiss shapefile
     */
    public static File getSwissShapefilePath() {
        Path dataDir = Paths.get(getDataDirectory());
        return dataDir.resolve("switzerland/swissBOUNDARIES3D_1_5_TLM_HOHEITSGEBIET.shp").toFile();
    }

    /**
     * Get the absolute path to the German shapefile
     */
    public static File getGermanShapefilePath() {
        Path dataDir = Paths.get(getDataDirectory());
        return dataDir.resolve("germany/Shapefile_Zensus2022/EPSG_25832/VG250_GEM.shp").toFile();
    }
}