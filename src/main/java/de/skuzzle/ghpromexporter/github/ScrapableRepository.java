package de.skuzzle.ghpromexporter.github;

import static de.skuzzle.ghpromexporter.github.ThrowingSupplier.unchecked;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.kohsuke.github.GitHub;

public final class ScrapableRepository {

    private final GHRepository repository;
    private final GHRepositoryStatistics statistics;

    private ScrapableRepository(GHRepository repository, GHRepositoryStatistics statistics) {
        this.repository = repository;
        this.statistics = statistics;
    }

    public static ScrapableRepository load(GitHubAuthentication authentication, String repositoryFullName) {
        try {
            final GitHub gitHub = authentication.connectToGithub();
            final GHRepository repository = gitHub.getRepository(repositoryFullName);
            return new ScrapableRepository(repository, repository.getStatistics());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
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

    public int size() {
        return repository.getSize();
    }

    public long totalAdditions() {
        return unchecked(statistics::getCodeFrequency).stream().mapToLong(CodeFrequency::getAdditions)
                .sum();
    }

    public long totalDeletions() {
        return -unchecked(statistics::getCodeFrequency).stream().mapToLong(CodeFrequency::getDeletions)
                .sum();
    }

}
