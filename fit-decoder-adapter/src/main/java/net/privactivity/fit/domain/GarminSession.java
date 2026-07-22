package net.privactivity.fit.domain;

import java.util.Objects;

public class GarminSession {
    Double maxAltitude;
    Short maxCadence;
    Short maxHeartRate;
    Double maxSpeed;
    Byte maxTemperature;
    Integer maxPower;

    Double avgAltitude;
    Short avgCadence;
    Short avgHeartRate;
    Double avgSpeed;
    Byte avgTemperature;
    Integer avgPower;

    Integer totalAscent;
    Integer totalDescent;
    Double totalDistance;
    Double totalMovingTime;
    Double totalElapsedTime;
    Integer totalCalories;

    Integer thresholdPower;
    Integer normalizedPower;

    String sport;
    private Float totalTrainingEffect;
    private Float trainingStressScore;
    private Float intensityFactor;
    private Byte avgLeftPco;
    private Byte avgRightPco;

    public GarminSession() {
    }

    public Double getMaxAltitude() {
        return this.maxAltitude;
    }

    public Short getMaxCadence() {
        return this.maxCadence;
    }

    public Short getMaxHeartRate() {
        return this.maxHeartRate;
    }

    public Double getMaxSpeed() {
        return this.maxSpeed;
    }

    public Byte getMaxTemperature() {
        return this.maxTemperature;
    }

    public Integer getMaxPower() {
        return this.maxPower;
    }

    public Double getAvgAltitude() {
        return this.avgAltitude;
    }

    public Short getAvgCadence() {
        return this.avgCadence;
    }

    public Short getAvgHeartRate() {
        return this.avgHeartRate;
    }

    public Double getAvgSpeed() {
        return this.avgSpeed;
    }

    public Byte getAvgTemperature() {
        return this.avgTemperature;
    }

    public Integer getAvgPower() {
        return this.avgPower;
    }

    public Integer getTotalAscent() {
        return this.totalAscent;
    }

    public Integer getTotalDescent() {
        return this.totalDescent;
    }

    public Double getTotalDistance() {
        return this.totalDistance;
    }

    public Double getTotalMovingTime() {
        return this.totalMovingTime;
    }

    public Double getTotalElapsedTime() {
        return this.totalElapsedTime;
    }

    public Integer getTotalCalories() {
        return this.totalCalories;
    }

    public Integer getThresholdPower() {
        return this.thresholdPower;
    }

    public Integer getNormalizedPower() {
        return this.normalizedPower;
    }

    public String getSport() {
        return this.sport;
    }

    public void setMaxAltitude(Double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public void setMaxCadence(Short maxCadence) {
        this.maxCadence = maxCadence;
    }

    public void setMaxHeartRate(Short maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMaxTemperature(Byte maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public void setMaxPower(Integer maxPower) {
        this.maxPower = maxPower;
    }

    public void setAvgAltitude(Double avgAltitude) {
        this.avgAltitude = avgAltitude;
    }

    public void setAvgCadence(Short avgCadence) {
        this.avgCadence = avgCadence;
    }

    public void setAvgHeartRate(Short avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void setAvgTemperature(Byte avgTemperature) {
        this.avgTemperature = avgTemperature;
    }

    public void setAvgPower(Integer avgPower) {
        this.avgPower = avgPower;
    }

    public void setTotalAscent(Integer totalAscent) {
        this.totalAscent = totalAscent;
    }

    public void setTotalDescent(Integer totalDescent) {
        this.totalDescent = totalDescent;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setTotalMovingTime(Double totalMovingTime) {
        this.totalMovingTime = totalMovingTime;
    }

    public void setTotalElapsedTime(Double totalElapsedTime) {
        this.totalElapsedTime = totalElapsedTime;
    }

    public void setTotalCalories(Integer totalCalories) {
        this.totalCalories = totalCalories;
    }

    public void setThresholdPower(Integer thresholdPower) {
        this.thresholdPower = thresholdPower;
    }

    public void setNormalizedPower(Integer normalizedPower) {
        this.normalizedPower = normalizedPower;
    }

    public void setTotalTrainingEffect(Float totalTrainingEffect) {
        this.totalTrainingEffect = totalTrainingEffect;
    }

    public Float getTotalTrainingEffect() {
        return totalTrainingEffect;
    }

    public void setTrainingStressScore(Float trainingStressScore) {
        this.trainingStressScore = trainingStressScore;
    }

    public Float getTrainingStressScore() {
        return trainingStressScore;
    }


    public void setIntensityFactor(Float intensityFactor) {
        this.intensityFactor = intensityFactor;
    }

    public Float getIntensityFactor() {
        return intensityFactor;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GarminSession other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$maxAltitude = this.getMaxAltitude();
        final Object other$maxAltitude = other.getMaxAltitude();
        if (!Objects.equals(this$maxAltitude, other$maxAltitude)) {
            return false;
        }
        final Object this$maxCadence = this.getMaxCadence();
        final Object other$maxCadence = other.getMaxCadence();
        if (!Objects.equals(this$maxCadence, other$maxCadence)) {
            return false;
        }
        final Object this$maxHeartRate = this.getMaxHeartRate();
        final Object other$maxHeartRate = other.getMaxHeartRate();
        if (!Objects.equals(this$maxHeartRate, other$maxHeartRate)) {
            return false;
        }
        final Object this$maxSpeed = this.getMaxSpeed();
        final Object other$maxSpeed = other.getMaxSpeed();
        if (!Objects.equals(this$maxSpeed, other$maxSpeed)) {
            return false;
        }
        final Object this$maxTemperature = this.getMaxTemperature();
        final Object other$maxTemperature = other.getMaxTemperature();
        if (!Objects.equals(this$maxTemperature, other$maxTemperature)) {
            return false;
        }
        final Object this$maxPower = this.getMaxPower();
        final Object other$maxPower = other.getMaxPower();
        if (!Objects.equals(this$maxPower, other$maxPower)) {
            return false;
        }
        final Object this$avgAltitude = this.getAvgAltitude();
        final Object other$avgAltitude = other.getAvgAltitude();
        if (!Objects.equals(this$avgAltitude, other$avgAltitude)) {
            return false;
        }
        final Object this$avgCadence = this.getAvgCadence();
        final Object other$avgCadence = other.getAvgCadence();
        if (!Objects.equals(this$avgCadence, other$avgCadence)) {
            return false;
        }
        final Object this$avgHeartRate = this.getAvgHeartRate();
        final Object other$avgHeartRate = other.getAvgHeartRate();
        if (!Objects.equals(this$avgHeartRate, other$avgHeartRate)) {
            return false;
        }
        final Object this$avgSpeed = this.getAvgSpeed();
        final Object other$avgSpeed = other.getAvgSpeed();
        if (!Objects.equals(this$avgSpeed, other$avgSpeed)) {
            return false;
        }
        final Object this$avgTemperature = this.getAvgTemperature();
        final Object other$avgTemperature = other.getAvgTemperature();
        if (!Objects.equals(this$avgTemperature, other$avgTemperature)) {
            return false;
        }
        final Object this$avgPower = this.getAvgPower();
        final Object other$avgPower = other.getAvgPower();
        if (!Objects.equals(this$avgPower, other$avgPower)) {
            return false;
        }
        final Object this$totalAscent = this.getTotalAscent();
        final Object other$totalAscent = other.getTotalAscent();
        if (!Objects.equals(this$totalAscent, other$totalAscent)) {
            return false;
        }
        final Object this$totalDescent = this.getTotalDescent();
        final Object other$totalDescent = other.getTotalDescent();
        if (!Objects.equals(this$totalDescent, other$totalDescent)) {
            return false;
        }
        final Object this$totalDistance = this.getTotalDistance();
        final Object other$totalDistance = other.getTotalDistance();
        if (!Objects.equals(this$totalDistance, other$totalDistance)) {
            return false;
        }
        final Object this$totalMovingTime = this.getTotalMovingTime();
        final Object other$totalMovingTime = other.getTotalMovingTime();
        if (!Objects.equals(this$totalMovingTime, other$totalMovingTime)) {
            return false;
        }
        final Object this$totalElapsedTime = this.getTotalElapsedTime();
        final Object other$totalElapsedTime = other.getTotalElapsedTime();
        if (!Objects.equals(this$totalElapsedTime, other$totalElapsedTime)) {
            return false;
        }
        final Object this$totalCalories = this.getTotalCalories();
        final Object other$totalCalories = other.getTotalCalories();
        if (!Objects.equals(this$totalCalories, other$totalCalories)) {
            return false;
        }
        final Object this$thresholdPower = this.getThresholdPower();
        final Object other$thresholdPower = other.getThresholdPower();
        if (!Objects.equals(this$thresholdPower, other$thresholdPower)) {
            return false;
        }
        final Object this$normalizedPower = this.getNormalizedPower();
        final Object other$normalizedPower = other.getNormalizedPower();
        if (!Objects.equals(this$normalizedPower, other$normalizedPower)) {
            return false;
        }
        final Object this$sport = this.getSport();
        final Object other$sport = other.getSport();
        return Objects.equals(this$sport, other$sport);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GarminSession;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $maxAltitude = this.getMaxAltitude();
        result = result * PRIME + ($maxAltitude == null ? 43 : $maxAltitude.hashCode());
        final Object $maxCadence = this.getMaxCadence();
        result = result * PRIME + ($maxCadence == null ? 43 : $maxCadence.hashCode());
        final Object $maxHeartRate = this.getMaxHeartRate();
        result = result * PRIME + ($maxHeartRate == null ? 43 : $maxHeartRate.hashCode());
        final Object $maxSpeed = this.getMaxSpeed();
        result = result * PRIME + ($maxSpeed == null ? 43 : $maxSpeed.hashCode());
        final Object $maxTemperature = this.getMaxTemperature();
        result = result * PRIME + ($maxTemperature == null ? 43 : $maxTemperature.hashCode());
        final Object $maxPower = this.getMaxPower();
        result = result * PRIME + ($maxPower == null ? 43 : $maxPower.hashCode());
        final Object $avgAltitude = this.getAvgAltitude();
        result = result * PRIME + ($avgAltitude == null ? 43 : $avgAltitude.hashCode());
        final Object $avgCadence = this.getAvgCadence();
        result = result * PRIME + ($avgCadence == null ? 43 : $avgCadence.hashCode());
        final Object $avgHeartRate = this.getAvgHeartRate();
        result = result * PRIME + ($avgHeartRate == null ? 43 : $avgHeartRate.hashCode());
        final Object $avgSpeed = this.getAvgSpeed();
        result = result * PRIME + ($avgSpeed == null ? 43 : $avgSpeed.hashCode());
        final Object $avgTemperature = this.getAvgTemperature();
        result = result * PRIME + ($avgTemperature == null ? 43 : $avgTemperature.hashCode());
        final Object $avgPower = this.getAvgPower();
        result = result * PRIME + ($avgPower == null ? 43 : $avgPower.hashCode());
        final Object $totalAscent = this.getTotalAscent();
        result = result * PRIME + ($totalAscent == null ? 43 : $totalAscent.hashCode());
        final Object $totalDescent = this.getTotalDescent();
        result = result * PRIME + ($totalDescent == null ? 43 : $totalDescent.hashCode());
        final Object $totalDistance = this.getTotalDistance();
        result = result * PRIME + ($totalDistance == null ? 43 : $totalDistance.hashCode());
        final Object $totalMovingTime = this.getTotalMovingTime();
        result = result * PRIME + ($totalMovingTime == null ? 43 : $totalMovingTime.hashCode());
        final Object $totalElapsedTime = this.getTotalElapsedTime();
        result = result * PRIME + ($totalElapsedTime == null ? 43 : $totalElapsedTime.hashCode());
        final Object $totalCalories = this.getTotalCalories();
        result = result * PRIME + ($totalCalories == null ? 43 : $totalCalories.hashCode());
        final Object $thresholdPower = this.getThresholdPower();
        result = result * PRIME + ($thresholdPower == null ? 43 : $thresholdPower.hashCode());
        final Object $normalizedPower = this.getNormalizedPower();
        result = result * PRIME + ($normalizedPower == null ? 43 : $normalizedPower.hashCode());
        final Object $sport = this.getSport();
        result = result * PRIME + ($sport == null ? 43 : $sport.hashCode());
        return result;
    }

    public String toString() {
        return "GarminSession(maxAltitude=" + this.getMaxAltitude() + ", maxCadence=" + this.getMaxCadence() + ", " +
               "maxHeartRate=" + this.getMaxHeartRate() + ", maxSpeed=" + this.getMaxSpeed() + ", maxTemperature=" + this.getMaxTemperature() + ", maxPower=" + this.getMaxPower() + ", avgAltitude=" + this.getAvgAltitude() + ", avgCadence=" + this.getAvgCadence() + ", avgHeartRate=" + this.getAvgHeartRate() + ", avgSpeed=" + this.getAvgSpeed() + ", avgTemperature=" + this.getAvgTemperature() + ", avgPower=" + this.getAvgPower() + ", totalAscent=" + this.getTotalAscent() + ", totalDescent=" + this.getTotalDescent() + ", totalDistance=" + this.getTotalDistance() + ", totalMovingTime=" + this.getTotalMovingTime() + ", totalElapsedTime=" + this.getTotalElapsedTime() + ", totalCalories=" + this.getTotalCalories() + ", thresholdPower=" + this.getThresholdPower() + ", normalizedPower=" + this.getNormalizedPower() + ", sport=" + this.getSport() + ")";
    }

    public void setAvgLeftPco(Byte avgLeftPco) {
        this.avgLeftPco = avgLeftPco;
    }

    public Byte getAvgLeftPco() {
        return avgLeftPco;
    }

    public void setAvgRightPco(Byte avgRightPco) {
        this.avgRightPco = avgRightPco;
    }

    public Byte getAvgRightPco() {
        return avgRightPco;
    }
}
