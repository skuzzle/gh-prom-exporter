package de.skuzzle.ghpromexporter.scrape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.github.ScrapableRepository;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.SimpleCollector.Builder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ScrapeRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(ScrapeRepositoryService.class);

    private static final String LABEL_REPOSITORY = "repository";
    private static final String LABEL_OWNER = "owner";
    private static final String NAMESPACE = "github";

    public Mono<RepositoryMetrics> scrapeRepository(ScrapeRepositoryRequest request) {
        return Mono.fromSupplier(() -> scrapeFresh(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private <B extends Builder<B, C>, C extends SimpleCollector<?>> C b(
            SimpleCollector.Builder<B, C> b,
            CollectorRegistry registry) {
        return b.namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
    }

    private RepositoryMetrics scrapeFresh(ScrapeRepositoryRequest repository) {
        final CollectorRegistry registry = new CollectorRegistry();

        final var authentication = repository.githubAuthentication();
        final var repositoryFullName = repository.repositoryFullName();
        final var scrapableRepository = ScrapableRepository.load(authentication, repositoryFullName);

        b(Counter.build("additions", "TBD"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.totalAdditions());
        b(Counter.build("deletions", "TBD"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.totalDeletions());
        b(Counter.build("stargazers", "The repository's stargazer count"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.stargazersCount());
        b(Counter.build("forks", "The repository's fork count"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.forkCount());
        b(Counter.build("open_issues", "The repository's open issue count"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.openIssueCount());
        b(Counter.build("subscribers", "The repository's subscriber count"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.subscriberCount());
        b(Counter.build("watchers", "The repository's watcher count"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.watchersCount());
        b(Counter.build("size", "The repository's size"), registry)
                .labels(repository.owner(), repository.name())
                .inc(scrapableRepository.size());

        final RepositoryMetrics metrics = RepositoryMetrics.fresh(repository, registry);
        log.info("Scraped fresh metrics for {}", repository);
        return metrics;
    }
}
