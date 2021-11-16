package de.skuzzle.ghpromexporter.github;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheConfiguration;

@ConfigurationProperties("github")
public final class GitHubProperties {

    private CacheConfiguration cache = new CacheConfiguration()
            .setMaximumSize(10000)
            .setExpireAfterAccess(Duration.ofHours(2));

    public CacheConfiguration cache() {
        return this.cache;
    }

    public void setCache(CacheConfiguration cache) {
        this.cache = cache;
    }
}
