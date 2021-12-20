package de.skuzzle.ghpromexporter.github;

import java.io.IOException;

import org.kohsuke.github.GitHub;

public class FailingGitHubAuthentication implements GitHubAuthentication {

    private static final long serialVersionUID = 1L;

    public static GitHubAuthentication failingAuthentication() {
        return new FailingGitHubAuthentication();
    }

    private FailingGitHubAuthentication() {
        // hidden
    }

    @Override
    public GitHub connectToGithub() throws IOException {
        throw new IOException("Failed to authenticate");
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }
}