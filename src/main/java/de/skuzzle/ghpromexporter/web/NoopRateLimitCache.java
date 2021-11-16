package de.skuzzle.ghpromexporter.web;

import org.springframework.stereotype.Component;

import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import reactor.core.publisher.Mono;

@Component
class NoopRateLimitCache implements RateLimitCache {

    @Override
    public Mono<Seat> tryAcquireSeat(ScrapeRepositoryRequest request) {
        return Mono.just(Seat.free()).filter(Seat::isFree);
    }

}
