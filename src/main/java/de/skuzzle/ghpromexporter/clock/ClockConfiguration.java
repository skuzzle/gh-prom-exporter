package de.skuzzle.ghpromexporter.clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

    @Bean
    ApplicationClock applicationClock() {
        return ApplicationClock.DEFAULT;
    }
}
