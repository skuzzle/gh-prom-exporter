package de.skuzzle.ghpromexporter.web;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheProperties;

@ConfigurationProperties("web")
public class WebProperties {

    private CacheProperties abuseCache = new CacheProperties()
            .setExpireAfterWrite(Duration.ofHours(6));
    private int abuseLimit = 3;

    public CacheProperties abuseCache() {
        return this.abuseCache;
    }

    public void setAbuseCache(CacheProperties abuseCache) {
        this.abuseCache = abuseCache;
    }

    public int abuseLimit() {
        return this.abuseLimit;
    }

    public void setAbuseLimit(int abuseLimit) {
        this.abuseLimit = abuseLimit;
    }
}
