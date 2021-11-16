package de.skuzzle.ghpromexporter.scrape;

import io.prometheus.client.CollectorRegistry;

public class RepositoryMetrics {

    private final ScrapeRepositoryRequest request;
    private final CollectorRegistry registry;

    private RepositoryMetrics(ScrapeRepositoryRequest request, CollectorRegistry registry) {
        this.request = request;
        this.registry = registry;
    }

    static RepositoryMetrics fresh(ScrapeRepositoryRequest request, CollectorRegistry registry) {
        return new RepositoryMetrics(request, registry);
    }

    public ScrapeRepositoryRequest request() {
        return this.request;
    }

    public CollectorRegistry registry() {
        return this.registry;
    }

}
