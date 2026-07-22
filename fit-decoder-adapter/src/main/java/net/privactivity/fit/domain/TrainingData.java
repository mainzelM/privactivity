package net.privactivity.fit.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TrainingData {
    private long id;
    private String title;
    private String description;
    private Date date;
    private Date addedDate;
    //@Transient

    private transient List<Record> records;
    private byte[] archive;
    private GarminSession garminSession;
    private GeoPolygon geoPolygon;
    private HrZones hrZones;
    // @Transient
    private String polyUrl;
    private byte[] polyData;
    // @DBRef
    private User user;
    // @DBRef
    private List<SegmentInfo> segmentInfoList;
    //for get_by_id aggregation
    private List<Segment> segmentInfos;

    public TrainingData() {
        this.records = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getDate() {
        return this.date;
    }

    public Date getAddedDate() {
        return this.addedDate;
    }

    public List<Record> getRecords() {
        return Collections.unmodifiableList(this.records);
    }

    public byte[] getArchive() {
        return this.archive;
    }

    public GarminSession getGarminSession() {
        return this.garminSession;
    }

    public GeoPolygon getGeoPolygon() {
        return this.geoPolygon;
    }

    public HrZones getHrZones() {
        return this.hrZones;
    }

    public String getPolyUrl() {
        return this.polyUrl;
    }

    public byte[] getPolyData() {
        return this.polyData;
    }

    public User getUser() {
        return this.user;
    }

    public List<SegmentInfo> getSegmentInfoList() {
        return this.segmentInfoList;
    }

    public List<Segment> getSegmentInfos() {
        return this.segmentInfos;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public void addRecord(Record record) {
        this.records.add(record);
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public void releaseRecords() {
        this.records = null;
    }

    public void setArchive(byte[] archive) {
        this.archive = archive;
    }

    public void setGarminSession(GarminSession garminSession) {
        this.garminSession = garminSession;
    }

    public void setGeoPolygon(GeoPolygon geoPolygon) {
        this.geoPolygon = geoPolygon;
    }

    public void setHrZones(HrZones hrZones) {
        this.hrZones = hrZones;
    }

    public void setPolyUrl(String polyUrl) {
        this.polyUrl = polyUrl;
    }

    public void setPolyData(byte[] polyData) {
        this.polyData = polyData;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSegmentInfoList(List<SegmentInfo> segmentInfoList) {
        this.segmentInfoList = segmentInfoList;
    }

    public void setSegmentInfos(List<Segment> segmentInfos) {
        this.segmentInfos = segmentInfos;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TrainingData other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getId() != other.getId()) {
            return false;
        }
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (!Objects.equals(this$title, other$title)) {
            return false;
        }
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (!Objects.equals(this$description, other$description)) {
            return false;
        }
        final Object this$date = this.getDate();
        final Object other$date = other.getDate();
        if (!Objects.equals(this$date, other$date)) {
            return false;
        }
        final Object this$addedDate = this.getAddedDate();
        final Object other$addedDate = other.getAddedDate();
        if (!Objects.equals(this$addedDate, other$addedDate)) {
            return false;
        }
        final Object this$records = this.records;
        final Object other$records = other.records;
        if (!Objects.equals(this$records, other$records)) {
            return false;
        }
        if (!java.util.Arrays.equals(this.getArchive(), other.getArchive())) {
            return false;
        }
        final Object this$garminSession = this.getGarminSession();
        final Object other$garminSession = other.getGarminSession();
        if (!Objects.equals(this$garminSession, other$garminSession)) {
            return false;
        }
        final Object this$geoPolygon = this.getGeoPolygon();
        final Object other$geoPolygon = other.getGeoPolygon();
        if (!Objects.equals(this$geoPolygon, other$geoPolygon)) {
            return false;
        }
        final Object this$hrZones = this.getHrZones();
        final Object other$hrZones = other.getHrZones();
        if (!Objects.equals(this$hrZones, other$hrZones)) {
            return false;
        }
        final Object this$polyUrl = this.getPolyUrl();
        final Object other$polyUrl = other.getPolyUrl();
        if (!Objects.equals(this$polyUrl, other$polyUrl)) {
            return false;
        }
        if (!java.util.Arrays.equals(this.getPolyData(), other.getPolyData())) {
            return false;
        }
        final Object this$user = this.getUser();
        final Object other$user = other.getUser();
        if (!Objects.equals(this$user, other$user)) {
            return false;
        }
        final Object this$segmentInfoList = this.getSegmentInfoList();
        final Object other$segmentInfoList = other.getSegmentInfoList();
        if (!Objects.equals(this$segmentInfoList, other$segmentInfoList)) {
            return false;
        }
        final Object this$segmentInfos = this.getSegmentInfos();
        final Object other$segmentInfos = other.getSegmentInfos();
        return Objects.equals(this$segmentInfos, other$segmentInfos);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TrainingData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $date = this.getDate();
        result = result * PRIME + ($date == null ? 43 : $date.hashCode());
        final Object $addedDate = this.getAddedDate();
        result = result * PRIME + ($addedDate == null ? 43 : $addedDate.hashCode());
        final Object $records = this.records;
        result = result * PRIME + ($records == null ? 43 : $records.hashCode());
        result = result * PRIME + java.util.Arrays.hashCode(this.getArchive());
        final Object $garminSession = this.getGarminSession();
        result = result * PRIME + ($garminSession == null ? 43 : $garminSession.hashCode());
        final Object $geoPolygon = this.getGeoPolygon();
        result = result * PRIME + ($geoPolygon == null ? 43 : $geoPolygon.hashCode());
        final Object $hrZones = this.getHrZones();
        result = result * PRIME + ($hrZones == null ? 43 : $hrZones.hashCode());
        final Object $polyUrl = this.getPolyUrl();
        result = result * PRIME + ($polyUrl == null ? 43 : $polyUrl.hashCode());
        result = result * PRIME + java.util.Arrays.hashCode(this.getPolyData());
        final Object $user = this.getUser();
        result = result * PRIME + ($user == null ? 43 : $user.hashCode());
        final Object $segmentInfoList = this.getSegmentInfoList();
        result = result * PRIME + ($segmentInfoList == null ? 43 : $segmentInfoList.hashCode());
        final Object $segmentInfos = this.getSegmentInfos();
        result = result * PRIME + ($segmentInfos == null ? 43 : $segmentInfos.hashCode());
        return result;
    }

    public String toString() {
        return "TrainingData(id=" + this.getId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", date=" + this.getDate() + ", addedDate=" + this.getAddedDate() + ", records=" + this.records + ", archive=" + java.util.Arrays.toString(this.getArchive()) + ", garminSession=" + this.getGarminSession() + ", geoPolygon=" + this.getGeoPolygon() + ", hrZones=" + this.getHrZones() + ", polyUrl=" + this.getPolyUrl() + ", polyData=" + java.util.Arrays.toString(this.getPolyData()) + ", user=" + this.getUser() + ", segmentInfoList=" + this.getSegmentInfoList() + ", segmentInfos=" + this.getSegmentInfos() + ")";
    }
}
