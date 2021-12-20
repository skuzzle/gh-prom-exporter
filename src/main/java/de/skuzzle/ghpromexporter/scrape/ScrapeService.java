package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.github.ScrapableRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
class ScrapeService {

    private static final Logger log = LoggerFactory.getLogger(ScrapeService.class);

    public Mono<RepositoryMetrics> scrapeReactive(GitHubAuthentication authentication,
            ScrapeRepositoryRequest repository) {
        return Mono.fromSupplier(() -> scrapeFresh(authentication, repository))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public RepositoryMetrics scrape(GitHubAuthentication authentication,
            ScrapeRepositoryRequest repository) {
        return scrapeFresh(authentication, repository);
    }

    private RepositoryMetrics scrapeFresh(GitHubAuthentication authentication, ScrapeRepositoryRequest repository) {
        final long start = System.currentTimeMillis();
        return AppMetrics.scrapeDuration().record(() -> {
            final var repositoryFullName = repository.repositoryFullName();
            final var scrapableRepository = ScrapableRepository.load(authentication, repositoryFullName);

            final RepositoryMetrics repositoryMetrics = new RepositoryMetrics(
                    scrapableRepository.totalAdditions(),
                    scrapableRepository.totalDeletions(),
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
