package de.skuzzle.ghpromexporter.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.server.reactive.ServerHttpRequest;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;

/**
 * Defines how to extract {@link GitHubAuthentication} from an incoming request. Mostly
 * needed to exchange the authentication with a mock implementation during tests.
 *
 * @author Simon Taddiken
 */
interface AuthenticationProvider {

    public static GitHubAuthentication parseRequest(ServerHttpRequest request) {
        final String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null) {
            if (authorization.toLowerCase().startsWith("basic ")) {
                final byte[] decodedBytes = Base64.getDecoder().decode(authorization.substring("basic ".length()));
                final String usernamePassword = new String(decodedBytes, StandardCharsets.ISO_8859_1);
                final String[] parts = usernamePassword.split(":");
                return GitHubAuthentication.usernamePassword(parts[0], parts[1]);
            } else if (authorization.toLowerCase().startsWith("token ")) {
                return GitHubAuthentication.token(authorization.substring("token ".length()));
            } else if (authorization.toLowerCase().startsWith("bearer ")) {
                return GitHubAuthentication.token(authorization.substring("bearer ".length()));
            }
        }
        return GitHubAuthentication.anonymous(request.getRemoteAddress().getAddress());
    }

    GitHubAuthentication authenticateRequest(ServerHttpRequest request);
}
