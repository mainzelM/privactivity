package net.privactivity.store.usecase.changetitle;

import net.privactivity.domain.Maybe;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTitleUseCaseTest {

    @Mock
    private MetaDataRepository metaDataRepository;

    private ChangeTitleUseCase testee;

    @BeforeEach
    void setUp() {
        testee = new ChangeTitleUseCase(metaDataRepository);
    }

    @Test
    void changeTitle_shouldCreateMetadataWhenMissing() {
        when(metaDataRepository.getMetaDataForActivity(42L)).thenReturn(Maybe.none());

        testee.changeTitle(42L, "Evening Ride");

        verify(metaDataRepository).setMetaData(42L, new MetaData("Evening Ride"));
    }

    @Test
    void changeTitle_shouldOverwriteExistingManualTitle() {
        when(metaDataRepository.getMetaDataForActivity(42L)).thenReturn(Maybe.some(new MetaData("Old Title")));

        testee.changeTitle(42L, "New Title");

        verify(metaDataRepository).setMetaData(42L, new MetaData("New Title"));
    }
}
