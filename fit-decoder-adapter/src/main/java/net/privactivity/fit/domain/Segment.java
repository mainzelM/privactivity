package net.privactivity.fit.domain;

import java.util.List;
import java.util.Objects;

public class Segment {
    private String id;
    private String name;
    private GeoLocation location;
    private Double distance;
    //@DBRef(lazy = true)
    private List<SegmentInfo> segmentInfoList;
    private List<SegmentInfo> topSegmentInfoList;
    private Long lowestTopTime;

    private List<User> userInfos;

    public Segment() {
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public GeoLocation getLocation() {
        return this.location;
    }

    public Double getDistance() {
        return this.distance;
    }

    public List<SegmentInfo> getSegmentInfoList() {
        return this.segmentInfoList;
    }

    public List<SegmentInfo> getTopSegmentInfoList() {
        return this.topSegmentInfoList;
    }

    public Long getLowestTopTime() {
        return this.lowestTopTime;
    }

    public List<User> getUserInfos() {
        return this.userInfos;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setSegmentInfoList(List<SegmentInfo> segmentInfoList) {
        this.segmentInfoList = segmentInfoList;
    }

    public void setTopSegmentInfoList(List<SegmentInfo> topSegmentInfoList) {
        this.topSegmentInfoList = topSegmentInfoList;
    }

    public void setLowestTopTime(Long lowestTopTime) {
        this.lowestTopTime = lowestTopTime;
    }

    public void setUserInfos(List<User> userInfos) {
        this.userInfos = userInfos;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Segment other)) {
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
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (!Objects.equals(this$name, other$name)) {
            return false;
        }
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (!Objects.equals(this$location, other$location)) {
            return false;
        }
        final Object this$distance = this.getDistance();
        final Object other$distance = other.getDistance();
        if (!Objects.equals(this$distance, other$distance)) {
            return false;
        }
        final Object this$segmentInfoList = this.getSegmentInfoList();
        final Object other$segmentInfoList = other.getSegmentInfoList();
        if (!Objects.equals(this$segmentInfoList, other$segmentInfoList)) {
            return false;
        }
        final Object this$topSegmentInfoList = this.getTopSegmentInfoList();
        final Object other$topSegmentInfoList = other.getTopSegmentInfoList();
        if (!Objects.equals(this$topSegmentInfoList, other$topSegmentInfoList)) {
            return false;
        }
        final Object this$lowestTopTime = this.getLowestTopTime();
        final Object other$lowestTopTime = other.getLowestTopTime();
        if (!Objects.equals(this$lowestTopTime, other$lowestTopTime)) {
            return false;
        }
        final Object this$userInfos = this.getUserInfos();
        final Object other$userInfos = other.getUserInfos();
        return Objects.equals(this$userInfos, other$userInfos);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Segment;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $distance = this.getDistance();
        result = result * PRIME + ($distance == null ? 43 : $distance.hashCode());
        final Object $segmentInfoList = this.getSegmentInfoList();
        result = result * PRIME + ($segmentInfoList == null ? 43 : $segmentInfoList.hashCode());
        final Object $topSegmentInfoList = this.getTopSegmentInfoList();
        result = result * PRIME + ($topSegmentInfoList == null ? 43 : $topSegmentInfoList.hashCode());
        final Object $lowestTopTime = this.getLowestTopTime();
        result = result * PRIME + ($lowestTopTime == null ? 43 : $lowestTopTime.hashCode());
        final Object $userInfos = this.getUserInfos();
        result = result * PRIME + ($userInfos == null ? 43 : $userInfos.hashCode());
        return result;
    }

    public String toString() {
        return "Segment(id=" + this.getId() + ", name=" + this.getName() + ", location=" + this.getLocation() + ", " +
               "distance=" + this.getDistance() + ", segmentInfoList=" + this.getSegmentInfoList() + ", " +
               "topSegmentInfoList=" + this.getTopSegmentInfoList() + ", lowestTopTime=" + this.getLowestTopTime() +
               ", userInfos=" + this.getUserInfos() + ")";
    }
}
