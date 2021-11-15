package de.skuzzle.promhub.ghpromexporter.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.kohsuke.github.GitHub;
import org.springframework.http.server.reactive.ServerHttpRequest;

sealed interface GitHubAuthentication {

    public static GitHubAuthentication fromRequest(ServerHttpRequest request) {
        final String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null) {
            if (authorization.toLowerCase().startsWith("basic ")) {
                final byte[] decodedBytes = Base64.getDecoder().decode(authorization.substring("basic ".length()));
                final String usernamePassword = new String(decodedBytes, StandardCharsets.ISO_8859_1);
                final String[] parts = usernamePassword.split(":");
                return new BasicAuthentication(parts[0], parts[1]);
            } else if (authorization.toLowerCase().startsWith("token ")) {
                return new TokenAuthentication(authorization.substring("token ".length()));
            } else if (authorization.toLowerCase().startsWith("bearer ")) {
                return new TokenAuthentication(authorization.substring("bearer ".length()));
            }
        }
        return new AnonymousAuthentication();
    }

    GitHub connectToGithub() throws IOException;

    public record TokenAuthentication(String token) implements GitHubAuthentication {

        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHub.connectUsingOAuth(token);
        }
    }

    public record BasicAuthentication(String username, String oauthToken) implements GitHubAuthentication {
        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHub.connect(username, oauthToken);
        }
    }

    public final class AnonymousAuthentication implements GitHubAuthentication {

        @Override
        public GitHub connectToGithub() throws IOException {
            return GitHub.connectAnonymously();
        }

    }
}
