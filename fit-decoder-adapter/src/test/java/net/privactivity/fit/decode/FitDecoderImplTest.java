package net.privactivity.fit.decode;

import net.privactivity.fit.domain.Record;
import net.privactivity.fit.domain.TrainingData;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;



import static org.assertj.core.api.Assertions.assertThat;

class FitDecoderImplTest {

    @Test
    void testDcodeActivityFixture() throws URISyntaxException, IOException {
        FitDecoderImpl testee = new FitDecoderImpl();
        Path fitFilePath = Path.of(Objects.requireNonNull(getClass().getResource("/Activity.fit")).toURI());

        TrainingData trainingData = testee.readTrainingData(Files.newInputStream(fitFilePath));

        List<Record> records = trainingData.getRecords();
        Record firstRecord = records.getFirst();
        Record lastRecord = records.getLast();

        assertThat(records).hasSize(3601);
        assertThat(trainingData.getDate()).isEqualTo(firstRecord.getTimestamp());
        assertThat(records).allSatisfy(record -> assertThat(record.getLapNumber()).isEqualTo(1));
        assertThat(lastRecord.getTimestamp().toInstant().getEpochSecond()
                   - firstRecord.getTimestamp().toInstant().getEpochSecond()).isEqualTo(3600L);

        assertThat(firstRecord.getDistance()).isEqualTo(0.0);
        assertThat(firstRecord.getCadence()).isEqualTo((short) 0);
        assertThat(firstRecord.getHeartRate()).isEqualTo((short) 126);

        assertThat(lastRecord.getDistance()).isEqualTo(3600.0);
        assertThat(lastRecord.getCadence()).isEqualTo((short) 30);
        assertThat(lastRecord.getHeartRate()).isEqualTo((short) 126);
    }
}
