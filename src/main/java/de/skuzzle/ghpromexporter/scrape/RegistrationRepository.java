package de.skuzzle.ghpromexporter.scrape;

import java.util.function.Function;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Manages scrape targets along with authentication information for each so that they can
 * be asynchronously iterated.
 *
 * @author Simon Taddiken
 */
interface RegistrationRepository {

    /**
     * Whether the repository contains any registered scrapers.
     *
     * @return Whether the repository is empty.
     */
    boolean isEmpty();

    /**
     * Estimated number of registered scrape targets.
     *
     * @return The number of scrapers.
     */
    int estimatedCount();

    /**
     * Returns the currently registered, non-expired scrapers as reactive stream.
     *
     * @return The scrapers.
     */
    Flux<RegisteredScraper> registeredScrapers();

    /**
     * Saves a freshly obtained scrape result for the given scraper.
     *
     * @param scraper The scraper.
     * @param freshResult The scrape result.
     */
    void updateRegistration(RegisteredScraper scraper, ScrapeResult freshResult);

    /**
     * Deletes the given single scraper from this repository.
     *
     * @param scraper The scraper to remove.
     */
    void deleteRegistration(RegisteredScraper scraper);

    /**
     * Deletes all registered scrapers from this repository.
     */
    void deleteAll();

    /**
     * Returns the most recent scrape result for the given scraper. If the repository
     * doesn't contain a result or the existing result expired, the given loader function
     * will be used to load and incorporate fresh metrics into this repository.
     *
     * @param scraper The scraper for which to retrieve the metrics.
     * @param loader Loader function for obtaining fresh metrics.
     * @return The metrics for the given scraper. Either freshly scraped or obtained from
     *         cache.
     */
    Mono<ScrapeResult> getExistingOrLoad(RegisteredScraper scraper,
            Function<RegisteredScraper, ScrapeResult> loader);

    /**
     * Combines a single scrape target (GitHub repository) along with authentication
     * information that are needed to access said target.
     *
     * @author Simon Taddiken
     */
    record RegisteredScraper(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {

        ScrapeResult scrapeWith(ScrapeService scrapeRepositoryService) {
            return scrapeRepositoryService.scrape(authentication, repository);
        }
    }
}
