package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;

final class GitHubFactory {

    private static final Logger log = LoggerFactory.getLogger(GitHubFactory.class);

    // Configured by GitHubConfiguration
    static volatile Cache<GitHubAuthentication, GitHub> CACHED_GITHUBS;

    public static GitHub createGitHub(InternalGitHubAuthentication origin) throws IOException {
        Preconditions.checkArgument(CACHED_GITHUBS != null, "GITHUB client cache has not yet been initialized");
        try {
            return CACHED_GITHUBS.get(origin, () -> buildGitHubFor(origin));
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof final IOException ioe) {
                throw ioe;
            }
            throw new IOException(e);
        }
    }

    private static GitHub buildGitHubFor(InternalGitHubAuthentication origin) throws IOException {
        log.info("Creating new GitHub connection for {}", origin);
        return origin.consumeBuilder(new GitHubBuilder())
                .withConnector(new RequestCountingHttpConnector())
                .build();
    }

    private static class RequestCountingHttpConnector implements GitHubConnector {

        @Override
        public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
            AppMetrics.apiCalls().increment();
            return DEFAULT.send(connectorRequest);
        }

    }

}
