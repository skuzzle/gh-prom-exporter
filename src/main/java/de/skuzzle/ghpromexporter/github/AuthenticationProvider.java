package de.skuzzle.ghpromexporter.github;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AuthenticationProvider {

    GitHubAuthentication authenticateRequest(ServerHttpRequest request);
}
