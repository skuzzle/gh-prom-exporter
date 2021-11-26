package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.github.ScrapableRepository;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
class ScrapeService {

    private static final Logger log = LoggerFactory.getLogger(ScrapeService.class);
    private static final String LABEL_REPOSITORY = "repository";
    private static final String LABEL_OWNER = "owner";
    private static final String NAMESPACE = "github";

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
        final Meters meters = new Meters();
        return AppMetrics.scrapeDuration().record(() -> {
            final var repositoryFullName = repository.repositoryFullName();
            final var scrapableRepository = ScrapableRepository.load(authentication, repositoryFullName);

            meters.additions.labels(repository.owner(), repository.name()).inc(scrapableRepository.totalAdditions());
            meters.deletions.labels(repository.owner(), repository.name()).inc(scrapableRepository.totalDeletions());
            meters.stargazers.labels(repository.owner(), repository.name()).inc(scrapableRepository.stargazersCount());
            meters.forks.labels(repository.owner(), repository.name()).inc(scrapableRepository.forkCount());
            meters.open_issues.labels(repository.owner(), repository.name()).inc(scrapableRepository.openIssueCount());
            meters.subscribers.labels(repository.owner(), repository.name()).inc(scrapableRepository.subscriberCount());
            meters.watchers.labels(repository.owner(), repository.name()).inc(scrapableRepository.watchersCount());
            meters.size.labels(repository.owner(), repository.name()).inc(scrapableRepository.size());
            final long scrapeDuration = System.currentTimeMillis() - start;
            meters.scrapeDuration.labels(repository.owner(), repository.name()).observe(scrapeDuration);

            final RepositoryMetrics metrics = RepositoryMetrics.fresh(repository, meters.registry, scrapeDuration);
            log.debug("Scraped fresh metrics for {} in {}ms", repository, scrapeDuration);
            return metrics;
        });
    }

    private static class Meters {
        private final CollectorRegistry registry;
        private final Counter additions;
        private final Counter deletions;
        private final Counter stargazers;
        private final Counter forks;
        private final Counter open_issues;
        private final Counter subscribers;
        private final Counter watchers;
        private final Counter size;
        private final Summary scrapeDuration;

        public Meters() {
            this.registry = new CollectorRegistry();
            this.additions = Counter.build("additions", "TBD")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.deletions = Counter.build("deletions", "TBD")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.stargazers = Counter.build("stargazers", "The repository's stargazer count")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.forks = Counter.build("forks", "The repository's fork count")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.open_issues = Counter.build("open_issues", "The repository's open issue count")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.subscribers = Counter.build("subscribers", "The repository's subscriber count")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.watchers = Counter.build("watchers", "The repository's watcher count")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.size = Counter.build("size", "The repository's size in KB")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
            this.scrapeDuration = Summary.build("scrape_duration", "Duration of a single scrape")
                    .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
        }

    }
}
