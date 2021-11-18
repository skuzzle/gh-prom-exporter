package de.skuzzle.ghpromexporter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.AsynchronousScrapeService;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import reactor.core.publisher.Mono;

@RestController
record PromController(AsynchronousScrapeService scrapeService, SerializedRegistryCache serializer) {

    @GetMapping(path = "{user}/{repo}")
    public Mono<ResponseEntity<String>> createStats(@PathVariable String user, @PathVariable String repo,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = GitHubAuthentication.fromRequest(request);
        if (gitHubAuthentication.isAnonymous()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Anonymous scraping is not allowed. Please include a GitHub API key in your request"));
        }
        final MediaType contentType = MediaType.TEXT_PLAIN;// determineContentType(request);
        final ScrapeRepositoryRequest scrapeRepositoryRequest = ScrapeRepositoryRequest.of(user, repo);

        return serializer.fromCache(scrapeRepositoryRequest, contentType)
                .switchIfEmpty(scrapeService.scrapeReactive(gitHubAuthentication, scrapeRepositoryRequest)
                        .map(result -> serializer.serializeRegistry(result, contentType)))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .contentType(contentType)
                        .body(serializedMetrics))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
                                .body("You have exceeded the local rate limit\n")));
    }
}
