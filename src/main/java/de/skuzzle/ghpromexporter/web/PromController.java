package de.skuzzle.ghpromexporter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryService;
import reactor.core.publisher.Mono;

@RestController
record PromController(ScrapeRepositoryService scrapeService, CachingRegistrySerializer serializer,
        RateLimitCache rateLimiter) {

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
