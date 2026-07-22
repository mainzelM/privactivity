package net.privactivity.store.unified;

import net.privactivity.domain.Bounds;

public record BoundsDTO(double south, double north, double west, double east) {
    public BoundsDTO(Bounds bounds) {
        this(bounds.south(), bounds.north(), bounds.west(), bounds.east());
    }
}