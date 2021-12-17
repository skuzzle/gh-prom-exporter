package de.skuzzle.ghpromexporter.scrape;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ScrapeProperties.class)
@EnableScheduling
public class ScrapeConfiguration {

    @Bean
    RegistrationRepository registrationRepository(ScrapeProperties properties) {
        return new MemoryRegistrationRepository(properties.cache().build());
    }

    @Bean
    AsynchronousScrapeService asynchronousScraper(RegistrationRepository registrationRepository,
            ScrapeService scrapeService,
            Tracer tracer) {
        return new AsynchronousScrapeService(registrationRepository, scrapeService, tracer);
    }
}
