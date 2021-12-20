package de.skuzzle.ghpromexporter.web;

import static de.skuzzle.ghpromexporter.github.FailingGitHubAuthentication.failingAuthentication;

import java.util.Objects;

import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.github.AuthenticationProvider;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;

@Primary
@Component
class MockableAuthenticationProvider implements AuthenticationProvider {

    private volatile GitHubAuthentication authentication = failingAuthentication();

    @Override
    public GitHubAuthentication authenticateRequest(ServerHttpRequest request) {
        return authentication;
    }

    public void with(GitHubAuthentication authentication, Runnable action) {
        try (var autoCloseMe = simulate(authentication)) {
            action.run();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AutoCloseable simulate(GitHubAuthentication authentication) {
        final GitHubAuthentication previous = this.authentication;
        this.authentication = Objects.requireNonNull(authentication, "authentication must not b e null");
        return () -> this.authentication = previous;
    }

}
