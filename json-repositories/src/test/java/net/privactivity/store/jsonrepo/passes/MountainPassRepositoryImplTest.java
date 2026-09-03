package net.privactivity.store.jsonrepo.passes;

import net.privactivity.domain.LatLon;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MountainPassRepositoryImplTest {

    private final MountainPassRepositoryImpl testee = new MountainPassRepositoryImpl();

    @Test
    void shouldReadAllCountrySpecificCsvsFromClasspath() {
        List<MountainPass> passes = testee.getAllPasses();

        assertThat(passes).hasSize(985);
        assertThat(passes).extracting(MountainPass::country)
                          .containsOnly("AT", "CH", "DE", "ES", "FR", "IT");
    }

    @Test
    void shouldMapPassesWithTheirCsvFilesCountryCode() {
        List<MountainPass> passes = testee.getAllPasses();

        MountainPass germanPass = findByName(passes, "Achtal / Engetal");
        assertThat(germanPass.country()).isEqualTo("DE");
        assertThat(germanPass.summitLatLon().lat()).isCloseTo(47.53661, within(0.000001));
        assertThat(germanPass.summitLatLon().lon()).isCloseTo(10.52133, within(0.000001));

        MountainPass frenchPass = findByName(passes, "Ballon d'Alsace");
        assertThat(frenchPass.country()).isEqualTo("FR");
        assertThat(frenchPass.summitLatLon()).isEqualTo(new LatLon(47.81972, 6.84039));

        Set<String> italianPassNames = Set.of("Alpe Camasca", "Alpe di Noveis");
        assertThat(passes.stream().filter(pass -> italianPassNames.contains(pass.name())).map(MountainPass::country))
                .containsOnly("IT");
    }

    private MountainPass findByName(List<MountainPass> passes, String name) {
        return passes.stream()
                     .filter(pass -> pass.name().equals(name))
                     .findFirst()
                     .orElseThrow(() -> new AssertionError("Pass not found: " + name));
    }
}
