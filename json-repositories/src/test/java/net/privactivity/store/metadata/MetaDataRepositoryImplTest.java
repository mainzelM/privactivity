package net.privactivity.store.metadata;

import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.jsonrepo.metadata.MetaDataRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;



import static org.assertj.core.api.Assertions.assertThat;

class MetaDataRepositoryImplTest {

    @TempDir
    private Path tempDir;

    private MetaDataRepositoryImpl testee;

    @BeforeEach
    void setUp() {
        testee = new MetaDataRepositoryImpl(tempDir.toString());
    }

    @Test
    void shouldStoreAndRetrieveMetadata() {
        MetaData originalMetadata = new MetaData("Test Activity Title");

        testee.setMetaData(123456L, originalMetadata);

        MetaData retrieved = testee.get(123456L);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.manualTitle().orThrow()).isEqualTo("Test Activity Title");

        List<MetaData> allMetadata = testee.getAll();
        assertThat(allMetadata).hasSize(1);
        assertThat(allMetadata.getFirst()).isEqualTo(originalMetadata);
    }

    @Test
    void shouldPersistToFileSystem() {
        MetaData metadata1 = new MetaData("Activity 1 Title");
        MetaData metadata2 = new MetaData("Activity 2 Title");

        testee.setMetaData(111L, metadata1);
        testee.setMetaData(222L, metadata2);

        MetaDataRepositoryImpl newStore = new MetaDataRepositoryImpl(tempDir.toString());

        List<MetaData> allMetadata = newStore.getAll();
        assertThat(allMetadata).hasSize(2);
        assertThat(allMetadata)
                .containsExactlyInAnyOrder(metadata1, metadata2);
    }

    @Test
    void shouldHandleEmptyRepository() {
        List<MetaData> allMetadata = testee.getAll();
        assertThat(allMetadata).isEmpty();

        MetaData nonExistent = testee.get(999L);
        assertThat(nonExistent).isNull();
    }

    @Test
    void shouldReadLegacyMetadataEntriesWithIdField() throws IOException {
        Files.writeString(tempDir.resolve("metadata.json"),
                          """
                          {
                            "123456": {
                              "id": 123456,
                              "manualTitle": {
                                "type": "some",
                                "value": "Legacy Activity Title"
                              }
                            }
                          }
                          """);

        MetaDataRepositoryImpl testee = new MetaDataRepositoryImpl(tempDir.toString());

        assertThat(testee.get(123456L)).isEqualTo(new MetaData("Legacy Activity Title"));
    }
}