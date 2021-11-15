package de.skuzzle.promhub.ghpromexporter.web;

import io.prometheus.client.CollectorRegistry;

class RepositoryMetrics {

    private final ScrapeRepositoryRequest request;
    private final CollectorRegistry registry;

    private RepositoryMetrics(ScrapeRepositoryRequest request, CollectorRegistry registry) {
        this.request = request;
        this.registry = registry;
    }

    public static RepositoryMetrics fresh(ScrapeRepositoryRequest request, CollectorRegistry registry) {
        return new RepositoryMetrics(request, registry);
    }

    public ScrapeRepositoryRequest request() {
        return this.request;
    }

    public CollectorRegistry registry() {
        return this.registry;
    }

}
