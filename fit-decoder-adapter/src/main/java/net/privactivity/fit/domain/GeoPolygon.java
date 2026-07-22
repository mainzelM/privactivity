package net.privactivity.fit.domain;

import java.util.List;
import java.util.Objects;

public class GeoPolygon {
    String type = "Polygon";
    List<List<Double[]>> coordinates;

    public GeoPolygon() {
    }

    public String getType() {
        return this.type;
    }

    public List<List<Double[]>> getCoordinates() {
        return this.coordinates;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCoordinates(List<List<Double[]>> coordinates) {
        this.coordinates = coordinates;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GeoPolygon other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (!Objects.equals(this$type, other$type)) {
            return false;
        }
        final Object this$coordinates = this.getCoordinates();
        final Object other$coordinates = other.getCoordinates();
        return Objects.equals(this$coordinates, other$coordinates);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GeoPolygon;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $coordinates = this.getCoordinates();
        result = result * PRIME + ($coordinates == null ? 43 : $coordinates.hashCode());
        return result;
    }

    public String toString() {
        return "GeoPolygon(type=" + this.getType() + ", coordinates=" + this.getCoordinates() + ")";
    }
}
