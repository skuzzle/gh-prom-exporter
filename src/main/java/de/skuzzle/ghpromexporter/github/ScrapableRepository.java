package de.skuzzle.ghpromexporter.github;

import static java.util.stream.Collectors.summarizingInt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.StreamSupport;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.kohsuke.github.GHRepositoryStatistics.ContributorStats;
import org.kohsuke.github.GitHub;

public final class ScrapableRepository {

    private final GHRepository repository;
    private final List<CodeFrequency> codeFrequency;
    private final int commitsToMainBranch;

    private ScrapableRepository(GHRepository repository, List<CodeFrequency> codeFrequency, int commitsToMainBranch) {
        this.repository = repository;
        this.codeFrequency = codeFrequency;
        this.commitsToMainBranch = commitsToMainBranch;
    }

    public static ScrapableRepository load(GitHubAuthentication authentication, String repositoryFullName) {
        try {
            final GitHub gitHub = authentication.connectToGithub();
            final GHRepository repository = gitHub.getRepository(repositoryFullName);
            final GHRepositoryStatistics statistics = repository.getStatistics();
            final List<CodeFrequency> codeFrequency = statistics.getCodeFrequency();
            final IntSummaryStatistics commitsToMainBranch = StreamSupport
                    .stream(statistics.getContributorStats().spliterator(), false)
                    .collect(summarizingInt(ContributorStats::getTotal));

            return new ScrapableRepository(repository, codeFrequency, (int) commitsToMainBranch.getSum());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public int stargazersCount() {
        return repository.getStargazersCount();
    }

    public int openIssueCount() {
        return repository.getOpenIssueCount();
    }

    public int subscriberCount() {
        return repository.getSubscribersCount();
    }

    public int watchersCount() {
        return repository.getWatchersCount();
    }

    public int forkCount() {
        return repository.getForksCount();
    }

    public int sizeInKb() {
        return repository.getSize();
    }

    public long totalAdditions() {
        return codeFrequency.stream().mapToLong(CodeFrequency::getAdditions).sum();
    }

    public long totalDeletions() {
        return -codeFrequency.stream().mapToLong(CodeFrequency::getDeletions).sum();
    }

    public int commitsToMainBranch() {
        return commitsToMainBranch;
    }

}
