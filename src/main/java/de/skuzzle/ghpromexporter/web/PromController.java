package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

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
record PromController(AsynchronousScrapeService scrapeService, SerializedRegistryCache serializer,
        AbuseLimiter abuseLimiter) {

    @GetMapping(path = "{user}/{repo}")
    public Mono<ResponseEntity<String>> createStats(@PathVariable String user, @PathVariable String repo,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = GitHubAuthentication.fromRequest(request);
        if (gitHubAuthentication.isAnonymous()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Anonymous scraping is not allowed. Please include a GitHub API key in your request"));
        }

        final InetAddress origin = request.getRemoteAddress().getAddress();
        final MediaType contentType = MediaType.TEXT_PLAIN;// determineContentType(request);
        final ScrapeRepositoryRequest scrapeRepositoryRequest = ScrapeRepositoryRequest.of(user, repo);

        return abuseLimiter.blockAbusers(origin)
                .flatMap(__ -> serializer.fromCache(scrapeRepositoryRequest, contentType)
                        .switchIfEmpty(
                                scrapeService.scrapeReactive(gitHubAuthentication, scrapeRepositoryRequest)
                                        .map(result -> serializer.serializeRegistry(scrapeRepositoryRequest,
                                                result.toRegistry(scrapeRepositoryRequest), contentType))))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .contentType(contentType)
                        .body(serializedMetrics))
                .doOnError(e -> abuseLimiter.recordCall(e, origin))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Your IP '%s' has exceeded the abuse limit\n".formatted(origin))));
    }
}
