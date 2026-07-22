package net.privactivity.fit.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Record implements Serializable {
    @Serial
    private static final long serialVersionUID = 3409192079555485500L;
    Short heartRate;
    Integer lat;
    Integer lon;
    Short cadence;
    Byte temperature; // C
    Double distance; // m
    Double speed; // m/s
    Double altitude; // m
    Double grade; // %
    Date timestamp;
    Integer power;
    Integer lapNumber;

    public Record() {
    }

    public Short getHeartRate() {
        return this.heartRate;
    }

    public Integer getLat() {
        return this.lat;
    }

    public Integer getLon() {
        return this.lon;
    }

    public Short getCadence() {
        return this.cadence;
    }

    public Byte getTemperature() {
        return this.temperature;
    }

    public Double getDistance() {
        return this.distance;
    }

    public Double getSpeed() {
        return this.speed;
    }

    public Double getAltitude() {
        return this.altitude;
    }

    public Double getGrade() {
        return this.grade;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public Integer getPower() {
        return this.power;
    }

    public Integer getLapNumber() {
        return this.lapNumber;
    }

    public void setHeartRate(Short heartRate) {
        this.heartRate = heartRate;
    }

    public void setLat(Integer lat) {
        this.lat = lat;
    }

    public void setLon(Integer lon) {
        this.lon = lon;
    }

    public void setCadence(Short cadence) {
        this.cadence = cadence;
    }

    public void setTemperature(Byte temperature) {
        this.temperature = temperature;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public void setLapNumber(Integer lapNumber) {
        this.lapNumber = lapNumber;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Record other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$heartRate = this.getHeartRate();
        final Object other$heartRate = other.getHeartRate();
        if (!Objects.equals(this$heartRate, other$heartRate)) {
            return false;
        }
        final Object this$lat = this.getLat();
        final Object other$lat = other.getLat();
        if (!Objects.equals(this$lat, other$lat)) {
            return false;
        }
        final Object this$lon = this.getLon();
        final Object other$lon = other.getLon();
        if (!Objects.equals(this$lon, other$lon)) {
            return false;
        }
        final Object this$cadence = this.getCadence();
        final Object other$cadence = other.getCadence();
        if (!Objects.equals(this$cadence, other$cadence)) {
            return false;
        }
        final Object this$temperature = this.getTemperature();
        final Object other$temperature = other.getTemperature();
        if (!Objects.equals(this$temperature, other$temperature)) {
            return false;
        }
        final Object this$distance = this.getDistance();
        final Object other$distance = other.getDistance();
        if (!Objects.equals(this$distance, other$distance)) {
            return false;
        }
        final Object this$speed = this.getSpeed();
        final Object other$speed = other.getSpeed();
        if (!Objects.equals(this$speed, other$speed)) {
            return false;
        }
        final Object this$altitude = this.getAltitude();
        final Object other$altitude = other.getAltitude();
        if (!Objects.equals(this$altitude, other$altitude)) {
            return false;
        }
        final Object this$grade = this.getGrade();
        final Object other$grade = other.getGrade();
        if (!Objects.equals(this$grade, other$grade)) {
            return false;
        }
        final Object this$timestamp = this.getTimestamp();
        final Object other$timestamp = other.getTimestamp();
        if (!Objects.equals(this$timestamp, other$timestamp)) {
            return false;
        }
        final Object this$power = this.getPower();
        final Object other$power = other.getPower();
        if (!Objects.equals(this$power, other$power)) {
            return false;
        }
        final Object this$lapNumber = this.getLapNumber();
        final Object other$lapNumber = other.getLapNumber();
        return Objects.equals(this$lapNumber, other$lapNumber);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Record;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $heartRate = this.getHeartRate();
        result = result * PRIME + ($heartRate == null ? 43 : $heartRate.hashCode());
        final Object $lat = this.getLat();
        result = result * PRIME + ($lat == null ? 43 : $lat.hashCode());
        final Object $lon = this.getLon();
        result = result * PRIME + ($lon == null ? 43 : $lon.hashCode());
        final Object $cadence = this.getCadence();
        result = result * PRIME + ($cadence == null ? 43 : $cadence.hashCode());
        final Object $temperature = this.getTemperature();
        result = result * PRIME + ($temperature == null ? 43 : $temperature.hashCode());
        final Object $distance = this.getDistance();
        result = result * PRIME + ($distance == null ? 43 : $distance.hashCode());
        final Object $speed = this.getSpeed();
        result = result * PRIME + ($speed == null ? 43 : $speed.hashCode());
        final Object $altitude = this.getAltitude();
        result = result * PRIME + ($altitude == null ? 43 : $altitude.hashCode());
        final Object $grade = this.getGrade();
        result = result * PRIME + ($grade == null ? 43 : $grade.hashCode());
        final Object $timestamp = this.getTimestamp();
        result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
        final Object $power = this.getPower();
        result = result * PRIME + ($power == null ? 43 : $power.hashCode());
        final Object $lapNumber = this.getLapNumber();
        result = result * PRIME + ($lapNumber == null ? 43 : $lapNumber.hashCode());
        return result;
    }

    public String toString() {
        return "Record(heartRate=" + this.getHeartRate() + ", lat=" + this.getLat() + ", lon=" + this.getLon() + ", " +
               "cadence=" + this.getCadence() + ", temperature=" + this.getTemperature() + ", distance=" + this.getDistance() + ", speed=" + this.getSpeed() + ", altitude=" + this.getAltitude() + ", grade=" + this.getGrade() + ", timestamp=" + this.getTimestamp() + ", power=" + this.getPower() + ", lapNumber=" + this.getLapNumber() + ")";
    }
}
