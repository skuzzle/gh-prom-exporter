package de.skuzzle.ghpromexporter.scrape;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheProperties;

@ConfigurationProperties(ScrapeProperties.PREFIX)
public class ScrapeProperties {

    static final String PREFIX = "scrape";
    static final String INTERVAL = PREFIX + ".interval";
    static final String INITIAL_DELAY = PREFIX + ".initialDelay";

    private CacheProperties cache = new CacheProperties();

    public CacheProperties cache() {
        return this.cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }
}
