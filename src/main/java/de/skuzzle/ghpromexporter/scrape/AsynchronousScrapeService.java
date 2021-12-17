package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.annotation.Scheduled;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.RegistrationRepository.RegisteredScraper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    public Mono<RepositoryMetrics> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest request) {
        final RegisteredScraper registeredScraper = new RegisteredScraper(authentication, request);

        return registrationRepository.getExistingOrLoad(registeredScraper, () -> {
            final RepositoryMetrics repositoryMetrics = registeredScraper.scrapeWith(scrapeRepositoryService);
            log.info("Cache miss for {}. Scraped fresh metrics now in {}ms", registeredScraper,
                    repositoryMetrics.scrapeDuration());
            return repositoryMetrics;
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    void scheduledScraping() {
        final Span newSpan = tracer.nextSpan().name("scheduledScrape");

        try (var ws = tracer.withSpan(newSpan.start())) {
            registrationRepository.registeredScrapers()
                    .doOnNext(scraper -> scrapeAndUpdateCache(newSpan, scraper))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            log.info("Updated cached metrics for all registered scrapers");
        } finally {
            newSpan.end();
        }
    }

    private void scrapeAndUpdateCache(Span parentSpan, RegisteredScraper scraper) {
        final Span nextSpan = tracer.nextSpan(parentSpan).name("scrapeSingleRepo");
        try (var ws = tracer.withSpan(nextSpan.start())) {
            final RepositoryMetrics repositoryMetrics = scraper.scrapeWith(scrapeRepositoryService);
            registrationRepository.updateRegistration(scraper, repositoryMetrics);
            log.info("Asynschronously updated metrics for: {} in {}ms", scraper, repositoryMetrics.scrapeDuration());
        } catch (final Exception e) {
            registrationRepository.deleteRegistration(scraper);
            log.error("Scrape using '{}' threw exception. Will be removed from cache of active scrapers", scraper, e);
        } finally {
            nextSpan.end();
        }
    }
}
