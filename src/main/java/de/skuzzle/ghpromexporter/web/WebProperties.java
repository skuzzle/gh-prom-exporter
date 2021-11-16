package de.skuzzle.ghpromexporter.web;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.skuzzle.ghpromexporter.cache.CacheProperties;

@ConfigurationProperties("web")
public class WebProperties {

    private CacheProperties serializedRegistryCache = new CacheProperties()
            .setExpireAfterWrite(Duration.ofMinutes(5));

    public CacheProperties serializedRegistryCache() {
        return this.serializedRegistryCache;
    }

    public void setSerializedRegistryCache(CacheProperties serializedRegistryCache) {
        this.serializedRegistryCache = serializedRegistryCache;
    }
}
