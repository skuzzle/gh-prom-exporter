package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.kohsuke.github.GitHub;
import org.springframework.http.server.reactive.ServerHttpRequest;

import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.AnonymousAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.BasicAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.TokenAuthentication;

public interface GitHubAuthentication {

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
        return new AnonymousAuthentication(request.getRemoteAddress().getAddress());
    }

    GitHub connectToGithub() throws IOException;

}
