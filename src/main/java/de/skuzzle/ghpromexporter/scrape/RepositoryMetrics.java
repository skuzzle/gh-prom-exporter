package de.skuzzle.ghpromexporter.scrape;

public record RepositoryMetrics(
        long totalAdditions,
        long totalDeletions,
        int stargazersCount,
        int forkCount,
        int openIssueCount,
        int subscriberCount,
        int watchersCount,
        int sizeInKb,
        long scrapeDuration) {}
