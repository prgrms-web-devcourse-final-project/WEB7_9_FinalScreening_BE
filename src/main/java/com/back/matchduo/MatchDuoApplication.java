package com.back.matchduo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("com.back.matchduo")
@EnableScheduling
public class MatchDuoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchDuoApplication.class, args);
    }

}
