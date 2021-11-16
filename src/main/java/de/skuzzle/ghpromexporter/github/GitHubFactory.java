package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

final class GitHubFactory {

    private static final Logger log = LoggerFactory.getLogger(GitHubFactory.class);

    private static final Cache<GitHubAuthentication, GitHub> CACHED_GITHUBS = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    public static GitHub createGitHub(InternalGitHubAuthentication origin) throws IOException {
        try {
            return CACHED_GITHUBS.get(origin, () -> buildGitHubFor(origin));
        } catch (final ExecutionException e) {
            if (e.getCause()instanceof final IOException ioe) {
                throw ioe;
            }
            throw new IOException(e);
        }
    }

    private static GitHub buildGitHubFor(InternalGitHubAuthentication origin) throws IOException {
        log.info("Creating new GitHub connection for {}", origin);
        return origin.consumeBuilder(new GitHubBuilder())
                .withRateLimitHandler(new LoggingRateLimiter(origin))
                .build();

    }

}
