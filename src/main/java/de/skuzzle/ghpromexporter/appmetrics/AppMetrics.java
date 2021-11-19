package de.skuzzle.ghpromexporter.appmetrics;

import io.prometheus.client.Summary;

/**
 * Internal metrics of this application, that not necessarily have to do with the metrics
 * scraped from GitHub.
 */
public final class AppMetrics {

    private static final String NAMESPACE = "ghp";

    public static final Summary SCRAPE_DURATION = Summary
            .build("scrape_duration_seconds", "Single repository scrape duration")
            .namespace(NAMESPACE)
            .register();

}
