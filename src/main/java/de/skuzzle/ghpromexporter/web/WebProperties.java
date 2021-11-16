package de.skuzzle.ghpromexporter.web;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheConfiguration;

@ConfigurationProperties("web")
public class WebProperties {

    private CacheConfiguration serializedRegistryCache = new CacheConfiguration()
            .setExpireAfterWrite(Duration.ofMinutes(5));

    public CacheConfiguration serializedRegistryCache() {
        return this.serializedRegistryCache;
    }

    public void setSerializedRegistryCache(CacheConfiguration serializedRegistryCache) {
        this.serializedRegistryCache = serializedRegistryCache;
    }
}
