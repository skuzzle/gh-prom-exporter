package de.skuzzle.ghpromexporter.scrape;

import static de.skuzzle.ghpromexporter.scrape.ThrowingSupplier.unchecked;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.kohsuke.github.GitHub;

class UncheckedRepository {

    private final GHRepository repository;
    private final GHRepositoryStatistics statistics;

    private UncheckedRepository(GHRepository repository, GHRepositoryStatistics statistics) {
        this.repository = repository;
        this.statistics = statistics;
    }

    public static UncheckedRepository load(ScrapeRepositoryRequest request) {
        try {
            final GitHub gitHub = request.githubAuthentication().connectToGithub();
            final GHRepository repository = gitHub.getRepository(request.repositoryFullName());
            return new UncheckedRepository(repository, repository.getStatistics());
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
