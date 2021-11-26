package de.skuzzle.ghpromexporter.appmetrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Internal metrics of this application, that not necessarily have to do with the metrics
 * scraped from GitHub.
 */
@Component
public final class AppMetrics {

    private static final String NAMESPACE = "ghp_";

    private static volatile Timer scrapeDuration;

    public AppMetrics(MeterRegistry registry) {
        scrapeDuration = Timer.builder(NAMESPACE + "repository_scrape_duration")
                .register(registry);
    }

    public static Timer scrapeDuration() {
        return scrapeDuration;
    }
}
