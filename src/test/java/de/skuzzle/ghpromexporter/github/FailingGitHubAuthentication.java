package de.skuzzle.ghpromexporter.github;

import java.io.IOException;

import org.kohsuke.github.GitHub;

public record FailingGitHubAuthentication(boolean anonymous) implements GitHubAuthentication {

    private static final long serialVersionUID = 1L;

    public static GitHubAuthentication failingAuthentication(boolean anonymous) {
        return new FailingGitHubAuthentication(anonymous);
    }

    public static GitHubAuthentication failingAuthentication() {
        return failingAuthentication(false);
    }

    @Override
    public GitHub connectToGithub() throws IOException {
        throw new IOException("Failed to authenticate");
    }

    @Override
    public boolean isAnonymous() {
        return anonymous;
    }
}