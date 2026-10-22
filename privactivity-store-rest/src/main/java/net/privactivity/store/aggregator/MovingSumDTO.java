package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.movingsum.ComputeMovingSumUseCase;

public record MovingSumDTO(int distanceInMeters, int movingTimeInHours, int ascentInKilometers) {

    MovingSumDTO(ComputeMovingSumUseCase.MovingSum movingSum) {
        this(movingSum.distanceInMeters(), movingSum.movingTimeInHours(), movingSum.ascentInKilometers());
    }
}
