package de.skuzzle.ghpromexporter.scrape;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

public record RepositoryMetrics(
        long totalAdditions,
        long totalDeletions,
        int stargazersCount,
        int forkCount,
        int openIssueCount,
        int subscriberCount,
        int watchersCount,
        int sizeKb,
        long scrapeDuration) {

    public CollectorRegistry toRegistry(ScrapeRepositoryRequest repository) {
        final Meters meters = new Meters();
        meters.additions.labels(repository.owner(), repository.name())
                .inc(totalAdditions());
        meters.deletions.labels(repository.owner(), repository.name())
                .inc(totalDeletions());
        meters.stargazers.labels(repository.owner(), repository.name()).inc(stargazersCount());
        meters.forks.labels(repository.owner(), repository.name()).inc(forkCount());
        meters.open_issues.labels(repository.owner(), repository.name()).inc(openIssueCount());
        meters.subscribers.labels(repository.owner(), repository.name()).inc(subscriberCount());
        meters.watchers.labels(repository.owner(), repository.name()).inc(watchersCount());
        meters.size.labels(repository.owner(), repository.name()).inc(sizeKb());
        meters.scrapeDuration.labels(repository.owner(), repository.name()).observe(scrapeDuration());
        return meters.registry;
    }

    private static class Meters {
        private static final String LABEL_REPOSITORY = "repository";
        private static final String LABEL_OWNER = "owner";
        private static final String NAMESPACE = "github";

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
