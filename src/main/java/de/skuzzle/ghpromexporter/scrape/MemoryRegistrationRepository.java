package de.skuzzle.ghpromexporter.scrape;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class MemoryRegistrationRepository implements RegistrationRepository {

    private final Cache<RegisteredScraper, RepositoryMetrics> registeredScrapers;

    public MemoryRegistrationRepository(Cache<RegisteredScraper, RepositoryMetrics> registeredScrapers) {
        this.registeredScrapers = registeredScrapers;
    }

    @Override
    public boolean isEmpty() {
        return registeredScrapers.size() == 0;
    }

    @Override
    public Flux<RegisteredScraper> registeredScrapers() {
        final Set<RegisteredScraper> scrapers = Set.copyOf(registeredScrapers.asMap().keySet());
        return Flux.fromIterable(scrapers);
    }

    @Override
    public void updateRegistration(RegisteredScraper scraper, RepositoryMetrics freshResult) {
        registeredScrapers.put(scraper, freshResult);
    }

    @Override
    public void deleteRegistration(RegisteredScraper scraper) {
        registeredScrapers.invalidate(scraper);
    }

    @Override
    public void deleteAll() {
        registeredScrapers.invalidateAll();
    }

    @Override
    public Mono<RepositoryMetrics> getExistingOrLoad(RegisteredScraper scraper, Supplier<RepositoryMetrics> loader) {
        return Mono.fromSupplier(() -> {
            try {
                return registeredScrapers.get(scraper, () -> loader.get());
            } catch (final ExecutionException e) {
                throw new RuntimeException(
                        "Error while loading fresh metrics after cache miss for " + scraper, e);
            }
        });
    }
}
