package de.skuzzle.ghpromexporter.scrape;

import io.prometheus.client.CollectorRegistry;

public final class ScrapeResult {

    private final ScrapeRepositoryRequest request;
    private final CollectorRegistry registry;
    private final long scrapeDuration;

    private ScrapeResult(ScrapeRepositoryRequest request, CollectorRegistry registry, long scrapeDuration) {
        this.request = request;
        this.registry = registry;
        this.scrapeDuration = scrapeDuration;
    }

    static ScrapeResult fresh(ScrapeRepositoryRequest request, CollectorRegistry registry, long scrapeDuration) {
        return new ScrapeResult(request, registry, scrapeDuration);
    }

    public ScrapeRepositoryRequest request() {
        return this.request;
    }

    public CollectorRegistry registry() {
        return this.registry;
    }

    public long scrapeDuration() {
        return this.scrapeDuration;
    }

}
