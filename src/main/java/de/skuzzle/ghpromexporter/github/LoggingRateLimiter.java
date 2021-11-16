package de.skuzzle.ghpromexporter.github;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.kohsuke.github.RateLimitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LoggingRateLimiter extends RateLimitHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingRateLimiter.class);

    private final GitHubAuthentication origin;

    public LoggingRateLimiter(GitHubAuthentication origin) {
        this.origin = origin;
    }

    @Override
    public void onError(IOException e, HttpURLConnection uc) throws IOException {
        log.warn("API rate limit has been hit for {}", origin);
        RateLimitHandler.WAIT.onError(e, uc);
    }

}
