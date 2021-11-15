package de.skuzzle.promhub.ghpromexporter.web;

import reactor.core.publisher.Mono;

public interface RateLimitCache {

    Mono<Seat> tryAcquireSeat(ScrapeRepositoryRequest request);

    static final class Seat {

        private final boolean free;

        private Seat(boolean free) {
            this.free = free;
        }

        public boolean isFree() {
            return this.free;
        }

        public static Seat free() {
            return new Seat(true);
        }

        public static Seat taken() {
            return new Seat(false);
        }

    }
}
