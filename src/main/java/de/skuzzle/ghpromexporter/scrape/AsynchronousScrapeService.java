package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.annotation.Scheduled;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.RegistrationRepository.RegisteredScraper;
import reactor.core.publisher.Mono;

/**
 *
 * @author Simon Taddiken
 */
public class AsynchronousScrapeService {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousScrapeService.class);

    private final RegistrationRepository registrationRepository;
    private final ScrapeService scrapeRepositoryService;
    private final Tracer tracer;

    AsynchronousScrapeService(RegistrationRepository registrationRepository,
            ScrapeService scrapeRepositoryService, Tracer tracer) {
        this.registrationRepository = registrationRepository;
        this.scrapeRepositoryService = scrapeRepositoryService;
        this.tracer = tracer;
    }

    public Mono<ScrapeResult> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest request) {
        final RegisteredScraper scrapeTarget = new RegisteredScraper(authentication, request);

        return registrationRepository
                .getExistingOrLoad(scrapeTarget, scraper -> {
                    final ScrapeResult repositoryMetrics = scraper.scrapeWith(scrapeRepositoryService);
                    log.info("Cache miss for {}. Scraped fresh metrics now in {}ms", scraper,
                            repositoryMetrics.scrapeDuration());
                    return repositoryMetrics;
                })
                .doOnError(error -> AppMetrics.scrapeFailures().increment());
    }

    @Scheduled(
            fixedDelayString = "${" + ScrapeProperties.INTERVAL + "}",
            initialDelayString = "${" + ScrapeProperties.INITIAL_DELAY + "}")
    void scheduledScraping() {
        log.debug("Running scheduled asynchronous scrape for all registered scrapers...");
        if (registrationRepository.isEmpty()) {
            // return early to improve INFO logging
            return;
        }
        AppMetrics.registeredScrapers().record(registrationRepository.estimatedCount());

        final Span newSpan = tracer.nextSpan().name("scheduledScrape");
        try (var ws = tracer.withSpan(newSpan.start())) {
            registrationRepository.registeredScrapers()
                    .doOnNext(scrapeTarget -> scrapeAndUpdateCache(newSpan, scrapeTarget))
                    .doOnTerminate(() -> log.info("Updated cached metrics for all registered scrapers"))
                    .blockLast();
        } finally {
            newSpan.end();
        }
    }

    private void scrapeAndUpdateCache(Span parentSpan, RegisteredScraper scrapeTarget) {
        final Span nextSpan = tracer.nextSpan(parentSpan).name("scrapeSingleRepo");
        try (var ws = tracer.withSpan(nextSpan.start())) {
            final ScrapeResult repositoryMetrics = scrapeTarget.scrapeWith(scrapeRepositoryService);
            registrationRepository.updateRegistration(scrapeTarget, repositoryMetrics);
            log.info("Asynschronously updated metrics for: {} in {}ms", scrapeTarget,
                    repositoryMetrics.scrapeDuration());
        } catch (final Exception e) {
            registrationRepository.deleteRegistration(scrapeTarget);
            AppMetrics.scrapeFailures().increment();
            log.error("Scrape using '{}' threw exception. Will be removed from cache of active scrapers", scrapeTarget,
                    e);
        } finally {
            nextSpan.end();
        }
    }
}
