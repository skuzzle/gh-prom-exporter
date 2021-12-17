package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.kohsuke.github.GitHub;

public final class ScrapableRepository {

    private final GHRepository repository;
    private final List<CodeFrequency> codeFrequency;

    private ScrapableRepository(GHRepository repository, List<CodeFrequency> codeFrequency) {
        this.repository = repository;
        this.codeFrequency = codeFrequency;
    }

    public static ScrapableRepository load(GitHubAuthentication authentication, String repositoryFullName) {
        try {
            final GitHub gitHub = authentication.connectToGithub();
            final GHRepository repository = gitHub.getRepository(repositoryFullName);
            final List<CodeFrequency> codeFrequency = repository.getStatistics().getCodeFrequency();

            return new ScrapableRepository(repository, codeFrequency);
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
        return codeFrequency.stream().mapToLong(CodeFrequency::getAdditions).sum();
    }

    public long totalDeletions() {
        return -codeFrequency.stream().mapToLong(CodeFrequency::getDeletions).sum();
    }

}
