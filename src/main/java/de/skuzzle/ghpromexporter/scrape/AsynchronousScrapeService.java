package de.skuzzle.ghpromexporter.scrape;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import reactor.core.publisher.Mono;

public class AsynchronousScrapeService {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousScrapeService.class);

    private final Cache<ActiveScraper, RepositoryMetrics> activeRequests;
    private final ScrapeService scrapeRepositoryService;
    private final Tracer tracer;

    AsynchronousScrapeService(Cache<ActiveScraper, RepositoryMetrics> activeRequests,
            ScrapeService scrapeRepositoryService, Tracer tracer) {
        this.activeRequests = activeRequests;
        this.scrapeRepositoryService = scrapeRepositoryService;
        this.tracer = tracer;
    }

    public Mono<RepositoryMetrics> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest request) {

        final ActiveScraper scraper = new ActiveScraper(authentication, request);
        return Mono.fromSupplier(() -> {
            try {
                return activeRequests.get(scraper, () -> {
                    final RepositoryMetrics repositoryMetrics = scraper.scrapeWith(scrapeRepositoryService);
                    log.info("Cache miss for {}. Scraped fresh metrics now in {}ms", scraper,
                            repositoryMetrics.scrapeDuration());
                    return repositoryMetrics;
                });
            } catch (final ExecutionException e) {
                throw new RuntimeException("Error while loading fresh metrics after cache miss for " + scraper, e);
            }
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    void scheduledScraping() {
        final Span newSpan = tracer.nextSpan().name("scheduledScrape");

        try (var ws = tracer.withSpan(newSpan.start())) {
            final Collection<ActiveScraper> scrapers = Set.copyOf(activeRequests.asMap().keySet());
            final int scraperCount = scrapers.stream()
                    .parallel()
                    .peek(scraper -> scrapeAndUpdateCache(newSpan, scraper))
                    .toList()
                    .size(); // dont use .count() because it doesnt execute the stream
                             // pipeline!
            log.info("Updated cached metrics for {} jobs", scraperCount);
        } finally {
            newSpan.end();
        }
    }

    private void scrapeAndUpdateCache(Span parentSpan, ActiveScraper scraper) {
        final Span nextSpan = tracer.nextSpan(parentSpan).name("scrapeSingleRepo");
        try (var ws = tracer.withSpan(nextSpan.start())) {
            final RepositoryMetrics repositoryMetrics = scraper.scrapeWith(scrapeRepositoryService);
            activeRequests.put(scraper, repositoryMetrics);
            log.info("Asynschronously updated metrics for: {} in {}ms", scraper, repositoryMetrics.scrapeDuration());
        } catch (final Exception e) {
            activeRequests.invalidate(scraper);
            log.error("Scrape using '{}' threw exception. Will be removed from cache of active scrapers", scraper, e);
        } finally {
            nextSpan.end();
        }
    }

    private record ActiveScraper(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {

        RepositoryMetrics scrapeWith(ScrapeService scrapeRepositoryService) {
            return scrapeRepositoryService.scrape(authentication, repository);
        }
    }
}
