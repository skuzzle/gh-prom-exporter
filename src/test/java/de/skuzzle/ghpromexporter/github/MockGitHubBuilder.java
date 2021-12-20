package de.skuzzle.ghpromexporter.github;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GitHub;
import org.mockito.Mockito;

public class MockGitHubBuilder {

    private final List<ThrowingConsumer<GitHub>> mockConsumers = new ArrayList<>();

    private MockGitHubBuilder() {
    }

    public static MockGitHubBuilder mockGitHub() {
        return new MockGitHubBuilder();
    }

    public MockGitHubBuilder withRepository(MockRepositoryBuilder repository) {
        this.mockConsumers.add(gh -> when(gh.getRepository(repository.getFullname())).thenReturn(repository.build()));
        return this;
    }

    public MockGitHubBuilder withInaccessibleRepository(MockRepositoryBuilder repository) {
        this.mockConsumers.add(gh -> when(gh.getRepository(repository.getFullname())).thenThrow(IOException.class));
        return this;
    }

    public GitHub build() {
        final GitHub mock = Mockito.mock(GitHub.class);
        mockConsumers.forEach(consumer -> consumer.acceptUnsafe(mock));
        return mock;
    }

    private interface ThrowingConsumer<T> {

        default void acceptUnsafe(T t) {
            try {
                accept(t);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        void accept(T t) throws Exception;
    }
}
