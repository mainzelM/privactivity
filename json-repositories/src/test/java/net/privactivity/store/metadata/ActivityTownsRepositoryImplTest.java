package net.privactivity.store.metadata;

import net.privactivity.domain.Activity;
import net.privactivity.store.jsonrepo.towns.ActivityTownsRepositoryImpl;
import net.privactivity.store.usecase.townfinder.model.Town;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;



import static org.assertj.core.api.Assertions.assertThat;

class ActivityTownsRepositoryImplTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldStoreAndReloadActivityTownsInOwnJsonFile() {
        ActivityTownsRepositoryImpl testee = new ActivityTownsRepositoryImpl(tempDir.toString());
        Activity activity = new Activity(123L, "Activity", null, List.of(), null);

        testee.setActivityTowns(activity, List.of("Berlin", "Potsdam"));

        ActivityTownsRepositoryImpl reloadedTestee = new ActivityTownsRepositoryImpl(tempDir.toString());

        assertThat(reloadedTestee.townsOfActivity(activity).orThrow())
                .containsExactly(new Town("Berlin"), new Town("Potsdam"));
        assertThat(tempDir.resolve("towns.json")).exists();
    }

    @Test
    void shouldReturnNoneWhenNoTownsExistForActivity() {
        ActivityTownsRepositoryImpl testee = new ActivityTownsRepositoryImpl(tempDir.toString());
        Activity activity = new Activity(456L, "Activity", null, List.of(), null);

        assertThat(testee.townsOfActivity(activity).isPresent()).isFalse();
    }
}
