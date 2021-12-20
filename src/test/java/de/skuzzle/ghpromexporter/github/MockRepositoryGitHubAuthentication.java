package de.skuzzle.ghpromexporter.github;

import java.io.IOException;

import org.kohsuke.github.GitHub;

public record MockRepositoryGitHubAuthentication(MockGitHubBuilder modify)
        implements GitHubAuthentication {

    public static MockRepositoryGitHubAuthentication successfulAuthenticationForRepository(
            MockRepositoryBuilder repository) {
        final MockGitHubBuilder gitHubBuilder = MockGitHubBuilder.mockGitHub()
                .withRepository(repository);
        return new MockRepositoryGitHubAuthentication(gitHubBuilder);
    }

    public static MockRepositoryGitHubAuthentication successfulAuthenticationForRepository(String name, String owner) {
        return successfulAuthenticationForRepository(MockRepositoryBuilder.withName(name, owner));
    }

    @Override
    public GitHub connectToGithub() throws IOException {
        return modify.build();
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

}
