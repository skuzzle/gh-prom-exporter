package de.skuzzle.ghpromexporter.scrape;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ScrapeProperties.class)
@EnableScheduling
public class ScrapeConfiguration {

    @Bean
    AsynchronousScrapeService asynchronousScraper(ScrapeProperties properties, ScrapeService scrapeService) {
        return new AsynchronousScrapeService(properties.cache().buildCache(), scrapeService);
    }
}
