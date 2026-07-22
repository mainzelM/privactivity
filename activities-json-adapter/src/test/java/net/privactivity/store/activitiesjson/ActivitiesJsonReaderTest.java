package net.privactivity.store.activitiesjson;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.List;



import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivitiesJsonReaderTest {

    ActivitiesJsonReaderImpl activitiesJsonReader = new ActivitiesJsonReaderImpl();

    @Test
    void read() {
        InputStream jsonStream = getClass().getResourceAsStream("/activities-1-809.json");
        List<GarminActivityImpl> activities =
                activitiesJsonReader.readFromStream(jsonStream);
        assertEquals(3, activities.size());
        assertEquals("Foo Cycling", activities.getFirst().getActivityName());
        assertEquals(2021, activities.getFirst().getStartTimeGMT().getYear());
    }
}