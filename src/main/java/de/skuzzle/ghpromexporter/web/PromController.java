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
import de.skuzzle.ghpromexporter.scrape.RepositoryMeters;
import io.prometheus.client.CollectorRegistry;
import reactor.core.publisher.Mono;

@RestController
record PromController(
        AuthenticationProvider authenticationProvider,
        AsynchronousScrapeService scrapeService,
        RegistrySerializer serializer,
        AbuseLimiter abuseLimiter,
        WebProperties properties) {

    @GetMapping(path = "{owner}/{repositories}")
    public Mono<ResponseEntity<String>> createStats(
            @PathVariable String owner,
            @PathVariable String repositories,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = authenticationProvider.authenticateRequest(request);

        if (gitHubAuthentication.isAnonymous() && !properties.allowAnonymousScrape()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Anonymous scraping is not allowed. Please include a GitHub API key in your request"));
        }

        final InetAddress origin = request.getRemoteAddress().getAddress();
        final MediaType contentType = MediaType.TEXT_PLAIN;// determineContentType(request);

        final MultipleRepositories multipleRepositories = MultipleRepositories.parse(owner, repositories);

        return abuseLimiter.blockAbusers(origin)
                .flatMap(__ -> freshResponse(gitHubAuthentication, multipleRepositories, contentType))
                .doOnError(e -> abuseLimiter.recordCall(e, origin))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Your IP '%s' has exceeded the abuse limit\n".formatted(origin))));
    }

    private Mono<ResponseEntity<String>> freshResponse(GitHubAuthentication authentication,
            MultipleRepositories repositories, MediaType contentType) {

        return scrapeAll(authentication, repositories)
                .map(registry -> serializer.serializeRegistry(registry, contentType))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .contentType(contentType)
                        .body(serializedMetrics));
    }

    private Mono<CollectorRegistry> scrapeAll(GitHubAuthentication authentication, MultipleRepositories repositories) {
        final RepositoryMeters meters = RepositoryMeters.newRegistry();

        return repositories.requests()
                .flatMap(req -> scrapeService.scrapeReactive(authentication, req)
                        .doOnNext(scrapeResult -> meters.addRepositoryScrapeResults(req, scrapeResult)))
                .then(Mono.just(meters.registry()));
    }
}
