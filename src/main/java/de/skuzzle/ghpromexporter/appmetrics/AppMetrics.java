package de.skuzzle.ghpromexporter.appmetrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * Internal metrics of this application, that not necessarily have to do with the metrics
 * scraped from GitHub.
 */
public final class AppMetrics {

    private static final String NAMESPACE = "ghp_";

    private static final Timer scrapeDuration = Metrics.timer(NAMESPACE + "repository_scrape_duration");

    public static Timer scrapeDuration() {
        return scrapeDuration;
    }
}
