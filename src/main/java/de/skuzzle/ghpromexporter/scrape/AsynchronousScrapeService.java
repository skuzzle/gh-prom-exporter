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

    public Mono<ScrapeResult> scrapeReactive(GitHubAuthentication authentication, ScrapeTarget scrapeTarget) {
        final RegisteredScraper registeredScraper = new RegisteredScraper(authentication, scrapeTarget);

        return registrationRepository
                .getExistingOrLoad(registeredScraper, scraper -> {
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
                    .doOnNext(registeredScraper -> scrapeAndUpdateCache(newSpan, registeredScraper))
                    .doOnTerminate(() -> log.info("Updated cached metrics for all registered scrapers"))
                    .blockLast();
        } finally {
            newSpan.end();
        }
    }

    private void scrapeAndUpdateCache(Span parentSpan, RegisteredScraper scraper) {
        final Span nextSpan = tracer.nextSpan(parentSpan).name("scrapeSingleRepo");
        try (var ws = tracer.withSpan(nextSpan.start())) {
            final ScrapeResult scrapeResult = scraper.scrapeWith(scrapeRepositoryService);
            registrationRepository.updateRegistration(scraper, scrapeResult);
            log.info("Asynschronously updated metrics for: {} in {}ms", scraper, scrapeResult.scrapeDuration());
        } catch (final Exception e) {
            registrationRepository.deleteRegistration(scraper);
            AppMetrics.scrapeFailures().increment();
            log.error("Scrape using '{}' threw exception. Will be removed from cache of active scrapers", scraper, e);
        } finally {
            nextSpan.end();
        }
    }
}
