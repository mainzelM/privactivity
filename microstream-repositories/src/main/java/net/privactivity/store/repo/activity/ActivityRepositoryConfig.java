package net.privactivity.store.repo.activity;

import org.eclipse.store.integrations.spring.boot.types.configuration.EclipseStoreProperties;
import org.eclipse.store.integrations.spring.boot.types.factories.EmbeddedStorageFoundationFactory;
import org.eclipse.store.integrations.spring.boot.types.factories.EmbeddedStorageManagerFactory;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivityRepositoryConfig {

    @Autowired
    private EmbeddedStorageFoundationFactory foundationFactory;
    @Autowired
    private EmbeddedStorageManagerFactory managerFactory;

    @Bean("activity_config")
    @ConfigurationProperties("privactivity.store.repo.activity")
    EclipseStoreProperties firstStoreProperties() {
        return new EclipseStoreProperties();
    }

    @Bean
    @Qualifier("activityStorageManager")
    EmbeddedStorageManager activityStorageManager(@Qualifier("activity_config") final EclipseStoreProperties fitStoreProperties) {
        return managerFactory.createStorage(
                foundationFactory.createStorageFoundation(fitStoreProperties),
                fitStoreProperties.isAutoStart());
    }
}