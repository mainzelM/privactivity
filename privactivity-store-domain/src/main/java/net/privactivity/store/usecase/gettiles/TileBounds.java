package net.privactivity.store.usecase.gettiles;

public record TileBounds(double south, double north, double west, double east) {

    public boolean intersects(TileBounds other) {
        return south <= other.north && north >= other.south && west <= other.east && east >= other.west;
    }
}