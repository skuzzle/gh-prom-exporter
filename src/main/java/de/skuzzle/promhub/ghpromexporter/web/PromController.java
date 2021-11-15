package de.skuzzle.promhub.ghpromexporter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
record PromController(Service scrapeService, RegistrySerializer serializer, RateLimitCache rateLimiter) {

    private static final Logger log = LoggerFactory.getLogger(PromController.class);

    @GetMapping(path = "{user}/{repo}")
    public Mono<ResponseEntity<String>> createStats(@PathVariable String user, @PathVariable String repo,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = GitHubAuthentication.fromRequest(request);
        final ApiKey apiKey = ApiKey.fromRequest(request);

        final MediaType contentType = MediaType.TEXT_PLAIN;// determineContentType(request);

        final ScrapeRepositoryRequest scrapeRepositoryRequest = ScrapeRepositoryRequest.of(user, repo,
                apiKey, gitHubAuthentication);

        return rateLimiter.tryAcquireSeat(scrapeRepositoryRequest)
                .flatMap(__ -> serializer.fromCache(scrapeRepositoryRequest, contentType)
                        .switchIfEmpty(scrapeService.scrapeRepository(scrapeRepositoryRequest)
                                .map(result -> serializer.serializeRegistry(result, contentType))))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .contentType(contentType)
                        .body(serializedMetrics))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
                                .body("You have exceeded the local rate limit\n")));
    }
}
