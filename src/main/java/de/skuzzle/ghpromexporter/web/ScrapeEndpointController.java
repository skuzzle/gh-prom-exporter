package de.skuzzle.ghpromexporter.web;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.AsynchronousScrapeService;
import de.skuzzle.ghpromexporter.scrape.PrometheusRepositoryMetricAggration;
import io.prometheus.client.CollectorRegistry;
import reactor.core.publisher.Mono;

@RestController
record ScrapeEndpointController(
        AuthenticationProvider authenticationProvider,
        AsynchronousScrapeService scrapeService,
        RegistrySerializer serializer,
        AbuseLimiter abuseLimiter,
        WebProperties properties) {

    private static final Logger log = LoggerFactory.getLogger(ScrapeEndpointController.class);

    @GetMapping(path = "{owner}/{repositories}")
    public Mono<ResponseEntity<String>> scrapeRepositories(
            @PathVariable String owner,
            @PathVariable String repositories,
            ServerHttpRequest request) {

        final GitHubAuthentication gitHubAuthentication = authenticationProvider.authenticateRequest(request);

        if (gitHubAuthentication.isAnonymous() && !properties.allowAnonymousScrape()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Anonymous scraping is not allowed. Please include a GitHub API key in your request"));
        }

        final InetAddress origin = request.getRemoteAddress().getAddress();
        final MediaType contentType = serializer.determineMediaType(request.getHeaders().getAccept());

        final var targets = MultipleScrapeTargets.parse(owner, repositories);
        log.info("Request from '{}' to scrape '{}'", gitHubAuthentication, targets);

        return abuseLimiter.blockAbusers(origin)
                .flatMap(__ -> scrapeRepositoriesAndSerialize(gitHubAuthentication, targets, contentType))
                .doOnError(exception -> abuseLimiter.recordFailedCall(exception, origin))
                .onErrorResume(exception -> Mono.just(ResponseEntity.badRequest().body(exception.getMessage())))
                .switchIfEmpty(
                        Mono.fromSupplier(() -> ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Your IP '%s' has exceeded the abuse limit".formatted(origin))));
    }

    private Mono<ResponseEntity<String>> scrapeRepositoriesAndSerialize(GitHubAuthentication authentication,
            MultipleScrapeTargets targets, MediaType contentType) {

        return scrapeRepositories(authentication, targets)
                .map(registry -> serializer.serializeRegistry(registry, contentType))
                .map(serializedMetrics -> ResponseEntity.ok()
                        .contentType(contentType)
                        .body(serializedMetrics));
    }

    private Mono<CollectorRegistry> scrapeRepositories(GitHubAuthentication authentication,
            MultipleScrapeTargets targets) {
        final PrometheusRepositoryMetricAggration meters = PrometheusRepositoryMetricAggration.newRegistry();

        return targets.requests()
                .flatMap(req -> scrapeService.scrapeReactive(authentication, req)
                        .doOnNext(scrapeResult -> meters.addRepositoryScrapeResults(req, scrapeResult)))
                .then(Mono.just(meters.registry()));
    }
}
