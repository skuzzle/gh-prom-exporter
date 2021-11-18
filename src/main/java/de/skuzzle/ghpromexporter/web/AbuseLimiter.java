package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

import reactor.core.publisher.Mono;

class AbuseLimiter {

    private static final Logger log = LoggerFactory.getLogger(AbuseLimiter.class);

    private final Cache<InetAddress, Integer> abusers;
    private final int abuseLimit;

    public AbuseLimiter(Cache<InetAddress, Integer> abusers, int abuseLimit) {
        this.abusers = abusers;
        this.abuseLimit = abuseLimit;
    }

    Mono<Object> blockAbusers(InetAddress origin) {
        return Mono.fromSupplier(() -> _0IfNull(abusers.getIfPresent(origin)))
                .filter(actualAbuses -> abuseLimitExceeded(origin, actualAbuses))
                .map(abuses -> (Object) abuses);
    }

    private boolean abuseLimitExceeded(InetAddress origin, int actualAbuses) {
        if (actualAbuses >= abuseLimit) {
            log.warn("Abuse limit exceeded for IP address {}. Countet violations: {}", origin, actualAbuses);
            return false;
        }
        return true;
    }

    void recordCall(Throwable e, InetAddress origin) {
        log.warn("Abuse recorded for {}: {}", origin, e.getMessage());
        abusers.put(origin, _0IfNull(abusers.getIfPresent(origin)) + 1);
    }

    private int _0IfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
