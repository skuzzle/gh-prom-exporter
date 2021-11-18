package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.net.InetAddress;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

sealed interface InternalGitHubAuthentication extends GitHubAuthentication {

    GitHubBuilder consumeBuilder(GitHubBuilder builder);

    record TokenAuthentication(String token) implements InternalGitHubAuthentication {

        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHubFactory.createGitHub(this);
        }

        @Override
        public GitHubBuilder consumeBuilder(GitHubBuilder builder) {
            return builder.withOAuthToken(token);
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public String toString() {
            return "token=****";
        }
    }

    record BasicAuthentication(String username, String oauthToken) implements InternalGitHubAuthentication {
        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHubFactory.createGitHub(this);
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public GitHubBuilder consumeBuilder(GitHubBuilder builder) {
            return builder.withOAuthToken(oauthToken, username);
        }

        @Override
        public String toString() {
            return "username=%s, token=****".formatted(username);
        }
    }

    record AnonymousAuthentication(InetAddress clientIp) implements InternalGitHubAuthentication {

        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHubFactory.createGitHub(this);
        }

        @Override
        public GitHubBuilder consumeBuilder(GitHubBuilder builder) {
            return builder;
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public String toString() {
            return "ip=%s".formatted(clientIp);
        }
    }
}
