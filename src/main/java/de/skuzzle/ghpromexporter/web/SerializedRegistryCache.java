package de.skuzzle.ghpromexporter.web;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.scrape.RepositoryMetrics;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import reactor.core.publisher.Mono;

class SerializedRegistryCache {

    private static final Logger log = LoggerFactory.getLogger(SerializedRegistryCache.class);

    private final Cache<CacheKey, String> cache;
    private final RegistrySerializer delegate;

    public SerializedRegistryCache(Cache<CacheKey, String> cache) {
        this.cache = cache;
        this.delegate = new RegistrySerializer();
    }

    public Mono<String> fromCache(ScrapeRepositoryRequest request, MediaType mediaType) {
        return Mono.fromSupplier(() -> {
            final CacheKey cacheKey = new CacheKey(mediaType, request.repositoryFullName());
            final String result = cache.getIfPresent(cacheKey);
            if (result != null) {
                log.debug("Resolved cached entry for {}", cacheKey);
            }
            return result;
        });
    }

    public String serializeRegistry(RepositoryMetrics metrics, MediaType mediaType) {
        final ScrapeRepositoryRequest request = metrics.request();
        try {
            return cache.get(
                    new CacheKey(mediaType, request.repositoryFullName()),
                    () -> delegate.serializeRegistry(metrics, mediaType));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private record CacheKey(MediaType mediaType, String repositoryName) {

    }
}
