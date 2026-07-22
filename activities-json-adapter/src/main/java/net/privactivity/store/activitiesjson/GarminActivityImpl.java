package net.privactivity.store.activitiesjson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.privactivity.store.usecase.importactivities.adapter.GarminActivity;

import java.time.LocalDateTime;

/**
 * Note that there are far more fields in the .json file
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GarminActivityImpl implements GarminActivity {
    private long activityId;
    private String activityName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeGMT;//"2021-07-22 06:02:21"
    private float distance;
    private float duration;
    private float movingDuration;
    private float elevationGain;
    private float elevationLoss;
    private float averageSpeed;
    private float maxSpeed;
    private float calories;
    private float averageHR;
    private float maxHR;
    private float avgPower;
    private float maxPower;
    private float maxElevation;

    @Override
    public long getActivityId() {
        return activityId;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    @Override
    public LocalDateTime getStartTimeGMT() {
        return startTimeGMT;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public float getMovingDuration() {
        return movingDuration;
    }

    @Override
    public float getElevationGain() {
        return elevationGain;
    }

    @Override
    public float getElevationLoss() {
        return elevationLoss;
    }

    @Override
    public float getAverageSpeed() {
        return averageSpeed;
    }

    @Override
    public float getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public float getAverageHR() {
        return averageHR;
    }

    @Override
    public float getMaxHR() {
        return maxHR;
    }

    @Override
    public float getAvgPower() {
        return avgPower;
    }

    @Override
    public float getMaxPower() {
        return maxPower;
    }

    @Override
    public float getMaxElevation() {
        return maxElevation;
    }
}
