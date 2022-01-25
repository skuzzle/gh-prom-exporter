package de.skuzzle.ghpromexporter.scrape;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.cache.Cache;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class MemoryRegistrationRepository implements RegistrationRepository {

    private final Cache<RegisteredScraper, RepositoryMetrics> registeredScrapers;

    public MemoryRegistrationRepository(Cache<RegisteredScraper, RepositoryMetrics> registeredScrapers) {
        this.registeredScrapers = registeredScrapers;
    }

    @Override
    public int estimatedCount() {
        return (int) registeredScrapers.size();
    }

    @Override
    public boolean isEmpty() {
        return registeredScrapers.size() == 0L;
    }

    @Override
    public Flux<RegisteredScraper> registeredScrapers() {
        final Set<RegisteredScraper> scrapers = Set.copyOf(registeredScrapers.asMap().keySet());
        return Flux.fromIterable(scrapers)
                .subscribeOn(Schedulers.boundedElastic());
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
    public Mono<RepositoryMetrics> getExistingOrLoad(RegisteredScraper scraper,
            Function<RegisteredScraper, RepositoryMetrics> loader) {
        return Mono.fromSupplier(() -> {
            try {
                return registeredScrapers.get(scraper, () -> loader.apply(scraper));
            } catch (final ExecutionException e) {
                throw new RuntimeException(
                        "Error while loading fresh metrics after cache miss for " + scraper, e);
            }
        });
    }
}
