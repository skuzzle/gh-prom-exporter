package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.kohsuke.github.GitHub;
import org.springframework.http.server.reactive.ServerHttpRequest;

import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.AnonymousAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.BasicAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.TokenAuthentication;

public sealed interface GitHubAuthentication extends Serializable permits InternalGitHubAuthentication {

    public static GitHubAuthentication fromRequest(ServerHttpRequest request) {
        final String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null) {
            if (authorization.toLowerCase().startsWith("basic ")) {
                final byte[] decodedBytes = Base64.getDecoder().decode(authorization.substring("basic ".length()));
                final String usernamePassword = new String(decodedBytes, StandardCharsets.ISO_8859_1);
                final String[] parts = usernamePassword.split(":");
                return usernamePassword(parts[0], parts[1]);
            } else if (authorization.toLowerCase().startsWith("token ")) {
                return token(authorization.substring("token ".length()));
            } else if (authorization.toLowerCase().startsWith("bearer ")) {
                return token(authorization.substring("bearer ".length()));
            }
        }
        return anonymous(request.getRemoteAddress().getAddress());
    }

    public static GitHubAuthentication usernamePassword(String username, String password) {
        return new BasicAuthentication(username, password);
    }

    public static GitHubAuthentication token(String token) {
        return new TokenAuthentication(token);
    }

    public static GitHubAuthentication anonymous(InetAddress source) {
        return new AnonymousAuthentication(source);
    }

    GitHub connectToGithub() throws IOException;

    boolean isAnonymous();

}
