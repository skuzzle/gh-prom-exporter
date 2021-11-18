package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

import com.google.common.cache.Cache;

import reactor.core.publisher.Mono;

class AbuseLimiter {

    private final Cache<InetAddress, Integer> abusers;
    private final int abuseLimit;

    public AbuseLimiter(Cache<InetAddress, Integer> abusers, int abuseLimit) {
        this.abusers = abusers;
        this.abuseLimit = abuseLimit;
    }

    Mono<Object> blockAbusers(InetAddress origin) {
        return Mono.fromSupplier(() -> _0IfNull(abusers.getIfPresent(origin)))
                .filter(abuses -> abuses < abuseLimit)
                .map(abuses -> (Object) abuses);
    }

    void recordCall(Throwable e, InetAddress origin) {
        abusers.put(origin, _0IfNull(abusers.getIfPresent(origin)) + 1);
    }

    private int _0IfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
