package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;

import de.skuzzle.ghpromexporter.appmetrics.AppMetrics;
import reactor.core.publisher.Mono;

class AbuseLimiter {

    private static final Logger log = LoggerFactory.getLogger(AbuseLimiter.class);

    private final Cache<InetAddress, Integer> abusers;
    private final int abuseLimit;

    public AbuseLimiter(Cache<InetAddress, Integer> abusers, int abuseLimit) {
        this.abusers = abusers;
        this.abuseLimit = abuseLimit;
    }

    /**
     * Returns an empty optional if the abuse limit was hit. Otherwise the Mono will
     * contain just a arbitrary object.
     *
     * @param origin The origin IP to check.
     * @return An empty Mono if the abuse limit was violated by that IP.
     */
    Mono<Object> blockAbusers(InetAddress origin) {
        return Mono.fromSupplier(() -> _0IfNull(abusers.getIfPresent(origin)))
                .filter(actualAbuses -> abuseLimitExceeded(origin, actualAbuses))
                .map(abuses -> (Object) abuses);
    }

    private boolean abuseLimitExceeded(InetAddress origin, int actualAbuses) {
        if (actualAbuses >= abuseLimit) {
            AppMetrics.abuses().increment();
            log.warn("Abuse limit exceeded for IP address {}. Countet violations: {}", origin, actualAbuses);
            return false;
        }
        return true;
    }

    /**
     * Records a potential abuse case for the given origin IP.
     *
     * @param e The error that occurred during request processing.
     * @param origin The origin IP.
     */
    void recordFailedCall(Throwable e, InetAddress origin) {
        log.warn("Abuse recorded for {}: {}", origin, e.getMessage());
        abusers.put(origin, _0IfNull(abusers.getIfPresent(origin)) + 1);
    }

    @VisibleForTesting
    void unblockAll() {
        abusers.invalidateAll();
    }

    private int _0IfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
