package de.skuzzle.promhub.ghpromexporter.web;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
class NoopRateLimitCache implements RateLimitCache {

    @Override
    public Mono<Seat> tryAcquireSeat(ScrapeRepositoryRequest request) {
        return Mono.just(Seat.free()).filter(Seat::isFree);
    }

}
