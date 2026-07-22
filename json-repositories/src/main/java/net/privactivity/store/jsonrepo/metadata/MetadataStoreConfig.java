package net.privactivity.store.jsonrepo.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataStoreConfig {

    @Bean
    @ConfigurationProperties("privactivity.store.metadata")
    MetadataStoreProperties metadataStoreProperties() {
        return new MetadataStoreProperties();
    }

    @Bean
    ObjectMapper metadataObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    String metadataStorageDirectory(MetadataStoreProperties properties) {
        return properties.getStorageDirectory();
    }

    public static class MetadataStoreProperties {
        private String storageDirectory;

        public String getStorageDirectory() {
            return storageDirectory;
        }

        public void setStorageDirectory(String storageDirectory) {
            this.storageDirectory = storageDirectory;
        }
    }
}