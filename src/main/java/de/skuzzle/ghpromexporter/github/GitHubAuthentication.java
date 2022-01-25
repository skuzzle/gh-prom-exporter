package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.net.InetAddress;

import org.kohsuke.github.GitHub;

import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.AnonymousAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.BasicAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.TokenAuthentication;

/**
 * Instances of this interface define how to connect and authenticate to GitHub.
 * Implementations are required to provide a consistent {@link #equals(Object)} and
 * {@link #hashCode()} implementation.
 *
 * @author Simon Taddiken
 */
public interface GitHubAuthentication {

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
