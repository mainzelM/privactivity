package net.privactivity.store.usecase.importactivities.adapter;

import java.time.LocalDateTime;

public interface GarminActivity {
    long getActivityId();

    String getActivityName();

    LocalDateTime getStartTimeGMT();

    float getDistance();

    float getMovingDuration();

    float getElevationGain();

    float getElevationLoss();

    float getAverageSpeed();

    float getMaxSpeed();

    float getAverageHR();

    float getMaxHR();

    float getAvgPower();

    float getMaxPower();

    float getMaxElevation();
}
