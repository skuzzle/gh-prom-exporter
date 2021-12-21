package de.skuzzle.ghpromexporter.web;

import static de.skuzzle.ghpromexporter.github.FailingGitHubAuthentication.failingAuthentication;
import static de.skuzzle.ghpromexporter.github.MockRepositoryBuilder.withName;
import static de.skuzzle.ghpromexporter.github.MockRepositoryGitHubAuthentication.successfulAuthenticationForRepository;
import static de.skuzzle.ghpromexporter.web.CanonicalPrometheusRegistrySerializer.canonicalPrometheusRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import de.skuzzle.ghpromexporter.github.FailingGitHubAuthentication;
import de.skuzzle.test.snapshots.SnapshotAssertions;
import de.skuzzle.test.snapshots.SnapshotDsl.Snapshot;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SnapshotAssertions(forceUpdateSnapshots = false)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "web.abuseCache.expireAfterWrite=1s")
public class PromControllerTest {

    @Autowired
    private MockableAuthenticationProvider authentication;
    @Autowired
    private WebProperties webProperties;
    @Autowired
    private AbuseLimiter abuseLimiter;
    @LocalServerPort
    private int localPort;

    @AfterEach
    void cleanup() {
        webProperties.setAllowAnonymousScrape(false);
        abuseLimiter.unblockAll();
    }

    private WebClient client() {
        return WebClient.builder()
                .baseUrl("http://localhost:" + localPort)
                .build();
    }

    private Mono<ResponseEntity<String>> getStatsFor(String owner, String repository) {
        return client().get().uri("/{owner}/{repository}", owner, repository)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .toEntity(String.class);
    }

    @Test
    void scrape_anonymously_forbidden() throws Exception {
        final var serviceCall = getStatsFor("skuzzle", "test-repo");

        authentication.with(FailingGitHubAuthentication.failingAuthentication(true), () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                    .verifyComplete();
        });
    }

    @Test
    void test_successful_initial_scrape(Snapshot snapshot) throws Exception {
        final var serviceCall = getStatsFor("skuzzle", "test-repo");

        authentication.with(successfulAuthenticationForRepository(withName("skuzzle", "test")
                .withStargazerCount(1337)
                .withForkCount(5)
                .withOpenIssueCount(2)
                .withWatchersCount(1)
                .withSubscriberCount(4)
                .withAdditions(50)
                .withDeletions(-20)
                .withSizeInKb(127)), () -> {

                    StepVerifier.create(serviceCall)
                            .assertNext(response -> snapshot.assertThat(response.getBody())
                                    .as(canonicalPrometheusRegistry())
                                    .matchesSnapshotText())
                            .verifyComplete();
                });
    }

    @Test
    void test_successful_anonymous_scrape(Snapshot snapshot) throws Exception {
        final var serviceCall = getStatsFor("skuzzle", "test-repo");
        webProperties.setAllowAnonymousScrape(true);

        authentication.with(successfulAuthenticationForRepository(withName("skuzzle", "test")
                .withStargazerCount(1337)
                .withForkCount(5)
                .withOpenIssueCount(2)
                .withWatchersCount(1)
                .withSubscriberCount(4)
                .withAdditions(50)
                .withDeletions(-20)
                .withSizeInKb(127)).setAnonymous(true), () -> {

                    StepVerifier.create(serviceCall)
                            .assertNext(response -> snapshot.assertThat(response.getBody())
                                    .as(canonicalPrometheusRegistry())
                                    .matchesSnapshotText())
                            .verifyComplete();
                });
    }

    @Test
    void client_should_be_blocked_when_abuse_limit_is_exceeded() throws Exception {
        final var serviceCall = getStatsFor("skuzzle", "test-repo");

        authentication.with(failingAuthentication(), () -> {
            for (int i = 0; i < webProperties.abuseLimit(); ++i) {
                StepVerifier.create(serviceCall)
                        .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                        .verifyComplete();
            }
        });

        authentication.with(successfulAuthenticationForRepository("skuzzle", "test-repo"), () -> {
            StepVerifier.create(serviceCall)
                    .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN))
                    .verifyComplete();
        });
    }

    @Test
    void client_should_be_unblocked_after_a_while() throws Exception {
        final var serviceCall = getStatsFor("skuzzle", "test-repo");

        authentication.with(failingAuthentication(), () -> {
            for (int i = 0; i < webProperties.abuseLimit(); ++i) {
                serviceCall.block();
            }
        });

        Thread.sleep(2000);

        authentication.with(successfulAuthenticationForRepository("skuzzle", "test-repo"), () -> {
            StepVerifier.create(serviceCall)
                    .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK))
                    .verifyComplete();
        });
    }
}
