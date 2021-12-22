package de.skuzzle.ghpromexporter.github;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.GitHub;
import org.mockito.Mockito;

public class MockGitHubBuilder {

    private final Map<String, ThrowingConsumer<GitHub>> perRepoConsumers = new HashMap<>();

    private MockGitHubBuilder() {
    }

    public static MockGitHubBuilder mockGitHub() {
        return new MockGitHubBuilder();
    }

    public MockGitHubBuilder withRepository(MockRepositoryBuilder repository) {
        perRepoConsumers.put(repository.fullname(),
                gh -> when(gh.getRepository(repository.fullname())).thenReturn(repository.build()));
        return this;
    }

    public MockGitHubBuilder withInaccessibleRepository(MockRepositoryBuilder repository) {
        perRepoConsumers.put(repository.fullname(),
                gh -> when(gh.getRepository(repository.fullname())).thenThrow(IOException.class));
        return this;
    }

    public GitHub build() {
        final GitHub mock = Mockito.mock(GitHub.class);
        perRepoConsumers.values().forEach(consumer -> consumer.acceptUnsafe(mock));
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
