package de.skuzzle.ghpromexporter.web;

import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.scrape.RepositoryMetrics;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import reactor.core.publisher.Mono;

class CachingRegistrySerializer {

    private static final MediaType OPEN_METRICS = MediaType
            .parseMediaType("application/openmetrics-text; version=1.0.0; charset=utf-8");

    private static final Logger log = LoggerFactory.getLogger(CachingRegistrySerializer.class);

    private final Cache<CacheKey, String> cache;

    public CachingRegistrySerializer(Cache<CacheKey, String> cache) {
        this.cache = cache;
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
            return cache.get(new CacheKey(mediaType, request.repositoryFullName()), () -> {
                final CollectorRegistry registry = metrics.registry();

                try (final var stringWriter = new StringWriter()) {
                    if (mediaType.equals(OPEN_METRICS)) {
                        TextFormat.writeOpenMetrics100(stringWriter, registry.metricFamilySamples());
                    } else {
                        TextFormat.write004(stringWriter, registry.metricFamilySamples());
                    }
                    return stringWriter.toString();
                }
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private record CacheKey(MediaType mediaType, String repositoryName) {

    }
}
