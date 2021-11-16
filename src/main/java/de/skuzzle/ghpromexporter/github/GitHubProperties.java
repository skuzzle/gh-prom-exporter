package de.skuzzle.ghpromexporter.github;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheProperties;

@ConfigurationProperties("github")
public final class GitHubProperties {

    private CacheProperties cache = new CacheProperties()
            .setMaximumSize(10000)
            .setExpireAfterAccess(Duration.ofHours(2));

    public CacheProperties cache() {
        return this.cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }
}
