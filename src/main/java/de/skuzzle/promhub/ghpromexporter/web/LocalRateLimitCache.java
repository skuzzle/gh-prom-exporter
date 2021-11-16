package de.skuzzle.promhub.ghpromexporter.web;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.publisher.Mono;

class LocalRateLimitCache implements RateLimitCache {

    private static final Logger log = LoggerFactory.getLogger(LocalRateLimitCache.class);

    private final Cache<Integer, CacheEntry> localCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(20))
            .build();

    @Override
    public Mono<Seat> tryAcquireSeat(ScrapeRepositoryRequest request) {
        return Mono.fromCallable(() -> {
            final LocalDateTime now = LocalDateTime.now();
            final int key = key(request);

            final CacheEntry cacheEntry = localCache.get(key, () -> new CacheEntry(now));

            // we have recently seen this user, test if rate limit is exceeded
            final Duration durationSinceLastCall = Duration.between(cacheEntry.lastCall(), now);
            if (Duration.ZERO.equals(durationSinceLastCall)) {
                // this is a cache miss, so its the very first observed call
                log.info("Free seat because first call (key={})", key);
                return Seat.free();
            }
            final Duration rateLimit = request.apiKey().rateLimit();
            if (durationSinceLastCall.compareTo(rateLimit) > 0) {
                log.info("Free seat because time since last call {} is longer ago than {} (key={})",
                        durationSinceLastCall,
                        rateLimit, key);
                localCache.put(key, new CacheEntry(now));
                return Seat.free();
            } else {
                log.info("Seat is taken because time since last call {} is not longer ago than {}",
                        durationSinceLastCall, rateLimit);
                return Seat.taken();
            }
        })
                .filter(Seat::isFree);
    }

    private int key(ScrapeRepositoryRequest request) {
        return Objects.hash(request.apiKey());
    }

    private record CacheEntry(LocalDateTime lastCall) {

    }

}
