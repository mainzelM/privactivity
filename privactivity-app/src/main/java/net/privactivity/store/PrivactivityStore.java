package net.privactivity.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrivactivityStore {

    static void main(String[] args) {
        SpringApplication.run(PrivactivityStore.class, args);
    }

}
