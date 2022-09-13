package de.skuzzle.ghpromexporter.web;

import java.util.Map;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@Lazy(true)
class TestClient {

    @LocalServerPort
    private int localPort;

    private WebClient client() {
        return WebClient.builder()
                .baseUrl("http://localhost:" + localPort)
                .build();
    }

    public Mono<ResponseEntity<String>> getStatsFor(String owner, String repository, Map<String, String> extraHeaders) {
        return client().get().uri("/{owner}/{repository}", owner, repository)
                .headers(requestHeaders -> extraHeaders.forEach(requestHeaders::add))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<String>> getStatsFor(String owner, String repository) {
        return client().get().uri("/{owner}/{repository}", owner, repository)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .toEntity(String.class);
    }
}
