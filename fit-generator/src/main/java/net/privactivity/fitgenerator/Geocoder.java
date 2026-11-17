package net.privactivity.fitgenerator;

public interface Geocoder {

    /**
     * @throws AmbiguousPlaceException if the name matches more than one place
     * @throws GenerationException     if the name matches nothing
     */
    Place geocode(String townName);
}
