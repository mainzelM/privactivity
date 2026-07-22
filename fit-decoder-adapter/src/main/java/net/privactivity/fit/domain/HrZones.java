package net.privactivity.fit.domain;

import java.util.Objects;

public class HrZones {
    private Integer zone1sec;
    private Integer zone2sec;
    private Integer zone3sec;
    private Integer zone4sec;
    private Integer zone5sec;
    private Integer maxHr;

    public HrZones() {
    }

    public Integer getZone1sec() {
        return this.zone1sec;
    }

    public Integer getZone2sec() {
        return this.zone2sec;
    }

    public Integer getZone3sec() {
        return this.zone3sec;
    }

    public Integer getZone4sec() {
        return this.zone4sec;
    }

    public Integer getZone5sec() {
        return this.zone5sec;
    }

    public Integer getMaxHr() {
        return this.maxHr;
    }

    public void setZone1sec(Integer zone1sec) {
        this.zone1sec = zone1sec;
    }

    public void setZone2sec(Integer zone2sec) {
        this.zone2sec = zone2sec;
    }

    public void setZone3sec(Integer zone3sec) {
        this.zone3sec = zone3sec;
    }

    public void setZone4sec(Integer zone4sec) {
        this.zone4sec = zone4sec;
    }

    public void setZone5sec(Integer zone5sec) {
        this.zone5sec = zone5sec;
    }

    public void setMaxHr(Integer maxHr) {
        this.maxHr = maxHr;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HrZones other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$zone1sec = this.getZone1sec();
        final Object other$zone1sec = other.getZone1sec();
        if (!Objects.equals(this$zone1sec, other$zone1sec)) {
            return false;
        }
        final Object this$zone2sec = this.getZone2sec();
        final Object other$zone2sec = other.getZone2sec();
        if (!Objects.equals(this$zone2sec, other$zone2sec)) {
            return false;
        }
        final Object this$zone3sec = this.getZone3sec();
        final Object other$zone3sec = other.getZone3sec();
        if (!Objects.equals(this$zone3sec, other$zone3sec)) {
            return false;
        }
        final Object this$zone4sec = this.getZone4sec();
        final Object other$zone4sec = other.getZone4sec();
        if (!Objects.equals(this$zone4sec, other$zone4sec)) {
            return false;
        }
        final Object this$zone5sec = this.getZone5sec();
        final Object other$zone5sec = other.getZone5sec();
        if (!Objects.equals(this$zone5sec, other$zone5sec)) {
            return false;
        }
        final Object this$maxHr = this.getMaxHr();
        final Object other$maxHr = other.getMaxHr();
        return Objects.equals(this$maxHr, other$maxHr);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HrZones;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $zone1sec = this.getZone1sec();
        result = result * PRIME + ($zone1sec == null ? 43 : $zone1sec.hashCode());
        final Object $zone2sec = this.getZone2sec();
        result = result * PRIME + ($zone2sec == null ? 43 : $zone2sec.hashCode());
        final Object $zone3sec = this.getZone3sec();
        result = result * PRIME + ($zone3sec == null ? 43 : $zone3sec.hashCode());
        final Object $zone4sec = this.getZone4sec();
        result = result * PRIME + ($zone4sec == null ? 43 : $zone4sec.hashCode());
        final Object $zone5sec = this.getZone5sec();
        result = result * PRIME + ($zone5sec == null ? 43 : $zone5sec.hashCode());
        final Object $maxHr = this.getMaxHr();
        result = result * PRIME + ($maxHr == null ? 43 : $maxHr.hashCode());
        return result;
    }

    public String toString() {
        return "HrZones(zone1sec=" + this.getZone1sec() + ", zone2sec=" + this.getZone2sec() + ", zone3sec=" + this.getZone3sec() + ", zone4sec=" + this.getZone4sec() + ", zone5sec=" + this.getZone5sec() + ", maxHr=" + this.getMaxHr() + ")";
    }
}
