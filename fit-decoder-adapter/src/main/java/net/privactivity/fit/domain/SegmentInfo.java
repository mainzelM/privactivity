package net.privactivity.fit.domain;

import java.util.Date;
import java.util.Objects;

public class SegmentInfo {
    private String id;
    private Long time;
    private Double avgSpeed;
    private Double maxSpeed;
    private Short avgHeart;
    private Short maxHeart;
    private Date date;
    private String userId;
    private String segmentId;
    private String trainingId;

    public SegmentInfo() {
    }

    public String getId() {
        return this.id;
    }

    public Long getTime() {
        return this.time;
    }

    public Double getAvgSpeed() {
        return this.avgSpeed;
    }

    public Double getMaxSpeed() {
        return this.maxSpeed;
    }

    public Short getAvgHeart() {
        return this.avgHeart;
    }

    public Short getMaxHeart() {
        return this.maxHeart;
    }

    public Date getDate() {
        return this.date;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getSegmentId() {
        return this.segmentId;
    }

    public String getTrainingId() {
        return this.trainingId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setAvgHeart(Short avgHeart) {
        this.avgHeart = avgHeart;
    }

    public void setMaxHeart(Short maxHeart) {
        this.maxHeart = maxHeart;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public void setTrainingId(String trainingId) {
        this.trainingId = trainingId;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SegmentInfo other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (!Objects.equals(this$id, other$id)) {
            return false;
        }
        final Object this$time = this.getTime();
        final Object other$time = other.getTime();
        if (!Objects.equals(this$time, other$time)) {
            return false;
        }
        final Object this$avgSpeed = this.getAvgSpeed();
        final Object other$avgSpeed = other.getAvgSpeed();
        if (!Objects.equals(this$avgSpeed, other$avgSpeed)) {
            return false;
        }
        final Object this$maxSpeed = this.getMaxSpeed();
        final Object other$maxSpeed = other.getMaxSpeed();
        if (!Objects.equals(this$maxSpeed, other$maxSpeed)) {
            return false;
        }
        final Object this$avgHeart = this.getAvgHeart();
        final Object other$avgHeart = other.getAvgHeart();
        if (!Objects.equals(this$avgHeart, other$avgHeart)) {
            return false;
        }
        final Object this$maxHeart = this.getMaxHeart();
        final Object other$maxHeart = other.getMaxHeart();
        if (!Objects.equals(this$maxHeart, other$maxHeart)) {
            return false;
        }
        final Object this$date = this.getDate();
        final Object other$date = other.getDate();
        if (!Objects.equals(this$date, other$date)) {
            return false;
        }
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (!Objects.equals(this$userId, other$userId)) {
            return false;
        }
        final Object this$segmentId = this.getSegmentId();
        final Object other$segmentId = other.getSegmentId();
        if (!Objects.equals(this$segmentId, other$segmentId)) {
            return false;
        }
        final Object this$trainingId = this.getTrainingId();
        final Object other$trainingId = other.getTrainingId();
        return Objects.equals(this$trainingId, other$trainingId);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SegmentInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $time = this.getTime();
        result = result * PRIME + ($time == null ? 43 : $time.hashCode());
        final Object $avgSpeed = this.getAvgSpeed();
        result = result * PRIME + ($avgSpeed == null ? 43 : $avgSpeed.hashCode());
        final Object $maxSpeed = this.getMaxSpeed();
        result = result * PRIME + ($maxSpeed == null ? 43 : $maxSpeed.hashCode());
        final Object $avgHeart = this.getAvgHeart();
        result = result * PRIME + ($avgHeart == null ? 43 : $avgHeart.hashCode());
        final Object $maxHeart = this.getMaxHeart();
        result = result * PRIME + ($maxHeart == null ? 43 : $maxHeart.hashCode());
        final Object $date = this.getDate();
        result = result * PRIME + ($date == null ? 43 : $date.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $segmentId = this.getSegmentId();
        result = result * PRIME + ($segmentId == null ? 43 : $segmentId.hashCode());
        final Object $trainingId = this.getTrainingId();
        result = result * PRIME + ($trainingId == null ? 43 : $trainingId.hashCode());
        return result;
    }

    public String toString() {
        return "SegmentInfo(id=" + this.getId() + ", time=" + this.getTime() + ", avgSpeed=" + this.getAvgSpeed() +
               ", maxSpeed=" + this.getMaxSpeed() + ", avgHeart=" + this.getAvgHeart() + ", maxHeart=" + this.getMaxHeart() + ", date=" + this.getDate() + ", userId=" + this.getUserId() + ", segmentId=" + this.getSegmentId() + ", trainingId=" + this.getTrainingId() + ")";
    }
}
