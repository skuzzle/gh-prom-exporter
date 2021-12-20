package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.skuzzle.ghpromexporter.github.AuthenticationProvider;
import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.AsynchronousScrapeService;
import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import reactor.core.publisher.Mono;

@RestController
record PromController(
        AuthenticationProvider authenticationProvider,
        AsynchronousScrapeService scrapeService,
        RegistrySerializer serializer,
        AbuseLimiter abuseLimiter) {

    private static final String CACHE_STATUS_HEADER = "X-Cache-Status";

    @GetMapping(path = "{user}/{repo}")
    public Mono<ResponseEntity<String>> createStats(
            @PathVariable String user,
            @PathVariable String repo,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = authenticationProvider.authenticateRequest(request);

        if (gitHubAuthentication.isAnonymous()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Anonymous scraping is not allowed. Please include a GitHub API key in your request"));
        }

        final InetAddress origin = request.getRemoteAddress().getAddress();
        final MediaType contentType = MediaType.TEXT_PLAIN;// determineContentType(request);
        final ScrapeRepositoryRequest scrapeRepositoryRequest = ScrapeRepositoryRequest.of(user, repo);

        return abuseLimiter.blockAbusers(origin)
                .flatMap(__ -> freshResponse(gitHubAuthentication, scrapeRepositoryRequest, contentType))
                .doOnError(e -> abuseLimiter.recordCall(e, origin))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Your IP '%s' has exceeded the abuse limit\n".formatted(origin))));
    }

    private Mono<ResponseEntity<String>> freshResponse(GitHubAuthentication authentication,
            ScrapeRepositoryRequest request, MediaType contentType) {
        return scrapeService.scrapeReactive(authentication, request)
                .map(result -> serializer.serializeRegistry(result.toRegistry(request), contentType))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .header(CACHE_STATUS_HEADER, "miss")
                        .contentType(contentType)
                        .body(serializedMetrics));
    }
}
