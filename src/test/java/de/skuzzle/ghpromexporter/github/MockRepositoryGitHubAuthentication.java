package de.skuzzle.ghpromexporter.github;

import java.io.IOException;

import org.kohsuke.github.GitHub;

public final class MockRepositoryGitHubAuthentication implements GitHubAuthentication {

    private final MockGitHubBuilder modify;
    private boolean anonymous = false;

    private MockRepositoryGitHubAuthentication(MockGitHubBuilder modify) {
        this.modify = modify;
    }

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

    public MockGitHubBuilder modify() {
        return this.modify;
    }

    public MockRepositoryGitHubAuthentication setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

}
