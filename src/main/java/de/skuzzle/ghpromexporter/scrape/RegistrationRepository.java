package de.skuzzle.ghpromexporter.scrape;

import com.google.common.base.Supplier;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface RegistrationRepository {

    boolean isEmpty();

    Flux<RegisteredScraper> registeredScrapers();

    void updateRegistration(RegisteredScraper scraper, RepositoryMetrics freshResult);

    void deleteRegistration(RegisteredScraper scraper);

    void deleteAll();

    Mono<RepositoryMetrics> getExistingOrLoad(RegisteredScraper scraper, Supplier<RepositoryMetrics> loader);

    record RegisteredScraper(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {

        RepositoryMetrics scrapeWith(ScrapeService scrapeRepositoryService) {
            return scrapeRepositoryService.scrape(authentication, repository);
        }
    }
}
