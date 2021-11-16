package de.skuzzle.ghpromexporter.scrape;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheProperties;

@ConfigurationProperties("scrape")
public class ScrapeProperties {

    private CacheProperties cache = new CacheProperties();

    public CacheProperties cache() {
        return this.cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }
}
