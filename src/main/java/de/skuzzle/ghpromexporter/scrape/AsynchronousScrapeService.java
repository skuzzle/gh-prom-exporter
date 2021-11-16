package de.skuzzle.ghpromexporter.scrape;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import reactor.core.publisher.Mono;

public class AsynchronousScrapeService {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousScrapeService.class);

    private final Cache<ActiveScraper, RepositoryMetrics> activeRequests;
    private final ScrapeService scrapeRepositoryService;

    public AsynchronousScrapeService(Cache<ActiveScraper, RepositoryMetrics> cache,
            ScrapeService scrapeRepositoryService) {
        this.activeRequests = cache;
        this.scrapeRepositoryService = scrapeRepositoryService;
    }

    public Mono<RepositoryMetrics> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest request) {

        final ActiveScraper scraper = new ActiveScraper(authentication, request);
        return Mono.fromSupplier(() -> {
            try {
                return activeRequests.get(scraper, () -> {
                    log.info("Cache miss for {}. Scraping fresh metrics now", scraper);
                    return scraper.scrapeWith(scrapeRepositoryService);
                });
            } catch (final ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    void scheduledScraping() {
        final Collection<ActiveScraper> scrapers = Set.copyOf(activeRequests.asMap().keySet());
        log.info("Running asynchronous scraper for {} jobs", scrapers.size());
        scrapers.stream().parallel().forEach(this::scrapeAndUpdateCache);
    }

    void scrapeAndUpdateCache(ActiveScraper scraper) {
        try {
            final RepositoryMetrics repositoryMetrics = scraper.scrapeWith(scrapeRepositoryService);
            activeRequests.put(scraper, repositoryMetrics);
            log.info("Asynschronously updated metrics for: {}", scraper);
        } catch (final Exception e) {
            activeRequests.invalidate(scraper);
            log.error("Scrape using '{}' threw exception. Will be removed from cache of active scrapers", scraper, e);
        }
    }

    private record ActiveScraper(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {

        RepositoryMetrics scrapeWith(ScrapeService scrapeRepositoryService) {
            return scrapeRepositoryService.scrape(authentication, repository);
        }
    }
}
