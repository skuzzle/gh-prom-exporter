package de.skuzzle.ghpromexporter.github;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.mockito.Mockito;

public class MockRepositoryBuilder {

    private final GHRepository repository = Mockito.mock(GHRepository.class);
    private final GHRepositoryStatistics statistics = Mockito.mock(GHRepositoryStatistics.class);
    private final List<CodeFrequency> codeFrequency = new ArrayList<>();

    private final String owner;
    private final String name;

    private MockRepositoryBuilder(String owner, String name) {
        this.owner = owner;
        this.name = name;
        try {
            when(repository.getOwnerName()).thenReturn(owner);
            when(repository.getName()).thenReturn(name);

            when(repository.getStatistics()).thenReturn(statistics);
            when(statistics.getCodeFrequency()).thenReturn(codeFrequency);
        } catch (final Exception ignore) {
            throw new IllegalStateException(ignore);
        }
    }

    public String getFullname() {
        return owner + "/" + name;
    }

    public static MockRepositoryBuilder withName(String owner, String name) {
        return new MockRepositoryBuilder(owner, name);
    }

    public MockRepositoryBuilder withStargazerCount(int count) {
        when(repository.getStargazersCount()).thenReturn(count);
        return this;
    }

    public MockRepositoryBuilder withForkCount(int count) {
        when(repository.getForksCount()).thenReturn(count);
        return this;
    }

    public MockRepositoryBuilder withOpenIssueCount(int count) {
        when(repository.getOpenIssueCount()).thenReturn(count);
        return this;
    }

    public MockRepositoryBuilder withSubscriberCount(int count) {
        when(repository.getSubscribersCount()).thenReturn(count);
        return this;
    }

    public MockRepositoryBuilder withWatchersCount(int count) {
        when(repository.getWatchersCount()).thenReturn(count);
        return this;
    }

    public MockRepositoryBuilder withSizeInKb(int sizeInKb) {
        when(repository.getSize()).thenReturn(sizeInKb);
        return this;
    }

    public MockRepositoryBuilder withAdditions(long additions) {
        final CodeFrequency frequency = mock(CodeFrequency.class);
        when(frequency.getAdditions()).thenReturn(additions);
        this.codeFrequency.add(frequency);
        return this;
    }

    public MockRepositoryBuilder withDeletions(long deletions) {
        final CodeFrequency frequency = mock(CodeFrequency.class);
        when(frequency.getDeletions()).thenReturn(deletions);
        this.codeFrequency.add(frequency);
        return this;
    }

    public GHRepository build() {
        return this.repository;
    }
}
