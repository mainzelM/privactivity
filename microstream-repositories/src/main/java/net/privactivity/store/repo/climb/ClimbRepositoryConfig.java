package net.privactivity.store.repo.climb;

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
public class ClimbRepositoryConfig {

    @Autowired
    private EmbeddedStorageFoundationFactory foundationFactory;
    @Autowired
    private EmbeddedStorageManagerFactory managerFactory;

    @Bean("climb_config")
    @ConfigurationProperties("privactivity.store.climb")
    EclipseStoreProperties climbStoreProperties() {
        return new EclipseStoreProperties();
    }

    @Bean
    @Qualifier("climb_storage")
    EmbeddedStorageManager climbStorageManager(@Qualifier("climb_config") final EclipseStoreProperties climbStoreProperties) {
        return managerFactory.createStorage(
                foundationFactory.createStorageFoundation(climbStoreProperties),
                climbStoreProperties.isAutoStart());
    }
}
