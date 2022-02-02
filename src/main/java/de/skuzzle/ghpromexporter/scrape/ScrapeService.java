package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.github.ScrapableRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Internal service for actually scraping a repository (given as
 * {@link ScrapeRepositoryRequest}), accessing it using a given
 * {@link GitHubAuthentication}.
 *
 * @author Simon Taddiken
 */
@Component
class ScrapeService {

    private static final Logger log = LoggerFactory.getLogger(ScrapeService.class);

    public Mono<ScrapeResult> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest repository) {
        return Mono.fromSupplier(() -> scrape(authentication, repository))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public ScrapeResult scrape(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {
        final long start = System.currentTimeMillis();
        return AppMetrics.scrapeDuration().record(() -> {
            final var repositoryFullName = repository.repositoryFullName();
            final var scrapableRepository = ScrapableRepository.load(authentication, repositoryFullName);

            final ScrapeResult repositoryMetrics = new ScrapeResult(
                    scrapableRepository.totalAdditions(),
                    scrapableRepository.totalDeletions(),
                    scrapableRepository.commitsToMainBranch(),
                    scrapableRepository.stargazersCount(),
                    scrapableRepository.forkCount(),
                    scrapableRepository.openIssueCount(),
                    scrapableRepository.subscriberCount(),
                    scrapableRepository.watchersCount(),
                    scrapableRepository.sizeInKb(),
                    System.currentTimeMillis() - start);

            log.debug("Scraped fresh metrics for {} in {}ms", repository, repositoryMetrics.scrapeDuration());
            return repositoryMetrics;
        });
    }
}
