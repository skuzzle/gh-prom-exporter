package de.skuzzle.ghpromexporter.scrape;

import java.io.Serializable;

import com.google.common.base.Supplier;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface RegistrationRepository {

    Flux<RegisteredScraper> registeredScrapers();

    void updateRegistration(RegisteredScraper scraper, RepositoryMetrics freshResult);

    void deleteRegistration(RegisteredScraper scraper);

    Mono<RepositoryMetrics> getExistingOrLoad(RegisteredScraper scraper, Supplier<RepositoryMetrics> loader);

    record RegisteredScraper(GitHubAuthentication authentication, ScrapeRepositoryRequest repository)
            implements Serializable {

        RepositoryMetrics scrapeWith(ScrapeService scrapeRepositoryService) {
            return scrapeRepositoryService.scrape(authentication, repository);
        }
    }
}
