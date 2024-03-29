package de.skuzzle.ghpromexporter.scrape;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

/**
 * Aggregates the scrape results of multiple repositories into a single Prometheus
 * registry.
 *
 * @author Simon Taddiken
 */
public final class PrometheusRepositoryMetricAggration {

    private static final String LABEL_REPOSITORY = "repository";
    private static final String LABEL_OWNER = "owner";
    private static final String NAMESPACE = "github";

    private final CollectorRegistry registry;
    private final Counter additions;
    private final Counter deletions;
    private final Counter commitsToMainBranch;
    private final Counter stargazers;
    private final Counter forks;
    private final Counter open_issues;
    private final Counter subscribers;
    private final Counter watchers;
    private final Counter size;
    private final Summary scrapeDuration;

    public static PrometheusRepositoryMetricAggration newRegistry() {
        return new PrometheusRepositoryMetricAggration();
    }

    private PrometheusRepositoryMetricAggration() {
        this.registry = new CollectorRegistry();
        this.additions = Counter.build("additions", "Sum of additions over the last 52 weeks")
                .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
        this.deletions = Counter.build("deletions", "Negative sum of deletions over the last 52 weeks")
                .namespace(NAMESPACE).labelNames(LABEL_OWNER, LABEL_REPOSITORY).register(registry);
        this.commitsToMainBranch = Counter.build("commits_to_main_branch", "Number of commits to the main branch")
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

    public PrometheusRepositoryMetricAggration addRepositoryScrapeResults(
            ScrapeTarget repository,
            ScrapeResult metrics) {

        if (metrics.statisticsAvailable()) {
            additions.labels(repository.owner(), repository.name()).inc(metrics.totalAdditions());
            deletions.labels(repository.owner(), repository.name()).inc(metrics.totalDeletions());
            commitsToMainBranch.labels(repository.owner(), repository.name()).inc(metrics.commitsToMainBranch());
        }

        stargazers.labels(repository.owner(), repository.name()).inc(metrics.stargazersCount());
        forks.labels(repository.owner(), repository.name()).inc(metrics.forkCount());
        open_issues.labels(repository.owner(), repository.name()).inc(metrics.openIssueCount());
        subscribers.labels(repository.owner(), repository.name()).inc(metrics.subscriberCount());
        watchers.labels(repository.owner(), repository.name()).inc(metrics.watchersCount());
        size.labels(repository.owner(), repository.name()).inc(metrics.sizeInKb());
        scrapeDuration.labels(repository.owner(), repository.name()).observe(metrics.scrapeDuration());
        return this;
    }

    public CollectorRegistry registry() {
        return this.registry;
    }
}
