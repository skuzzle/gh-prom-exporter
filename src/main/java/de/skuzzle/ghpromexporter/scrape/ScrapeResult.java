package de.skuzzle.ghpromexporter.scrape;

/**
 * Raw results of a single repository scrape.
 *
 * @author Simon Taddiken
 */
public record ScrapeResult(
        long totalAdditions,
        long totalDeletions,
        int commitsToMainBranch,
        int stargazersCount,
        int forkCount,
        int openIssueCount,
        int subscriberCount,
        int watchersCount,
        int sizeInKb,

        /* Note: this field must always occur last */
        long scrapeDuration) {}
