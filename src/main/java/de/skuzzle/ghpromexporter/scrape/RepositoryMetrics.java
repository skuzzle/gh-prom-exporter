package de.skuzzle.ghpromexporter.scrape;

/**
 * Raw results of a single repository scrape.
 *
 * @author Simon Taddiken
 */
public record RepositoryMetrics(
        long totalAdditions,
        long totalDeletions,
        int stargazersCount,
        int forkCount,
        int openIssueCount,
        int subscriberCount,
        int watchersCount,
        int sizeInKb,

        /* Note: this field must always occur last */
        long scrapeDuration) {}
