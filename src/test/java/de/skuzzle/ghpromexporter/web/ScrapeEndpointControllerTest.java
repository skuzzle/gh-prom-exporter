package de.skuzzle.ghpromexporter.web;

import static de.skuzzle.ghpromexporter.github.FailingGitHubAuthentication.failingAuthentication;
import static de.skuzzle.ghpromexporter.github.MockRepositoryBuilder.withName;
import static de.skuzzle.ghpromexporter.github.MockRepositoryGitHubAuthentication.successfulAuthenticationForRepository;
import static de.skuzzle.ghpromexporter.web.CanonicalPrometheusRegistrySerializer.canonicalPrometheusRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.test.snapshots.EnableSnapshotTests;
import de.skuzzle.test.snapshots.SnapshotDsl.Snapshot;
import reactor.test.StepVerifier;

@EnableSnapshotTests
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "web.abuseCache.expireAfterWrite=1s")
public class ScrapeEndpointControllerTest {

    @Autowired
    private MockableAuthenticationProvider authentication;
    @Autowired
    private WebProperties webProperties;
    @Autowired
    private AbuseLimiter abuseLimiter;
    @Autowired
    private TestClient testClient;

    @AfterEach
    void cleanup() {
        webProperties.setAllowAnonymousScrape(false);
        abuseLimiter.unblockAll();
    }

    @Test
    void scrape_anonymously_forbidden() throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");
        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withStargazerCount(1337))
                                .setAnonymous(true);

        authentication.with(gitHubAuthentication, () -> {
            StepVerifier.create(serviceCall)
                    .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                    .verifyComplete();
        });
    }

    @Test
    void scrape_multiple_repositories(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo1,test-repo2");
        final var gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo1")
                        .withForkCount(5));

        gitHubAuthentication.modify().withRepository(withName("skuzzle", "test-repo2").withStargazerCount(1337));
        authentication.with(gitHubAuthentication, () -> {
            StepVerifier.create(serviceCall)
                    .assertNext(response -> snapshot.assertThat(response.getBody())
                            .as(canonicalPrometheusRegistry())
                            .matchesSnapshotText())
                    .verifyComplete();
        });
    }

    @Test
    void test_successful_initial_scrape(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");
        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withStargazerCount(1337)
                        .withForkCount(5)
                        .withOpenIssueCount(2)
                        .withWatchersCount(1)
                        .withSubscriberCount(4)
                        .withAdditions(50)
                        .withDeletions(-20)
                        .withCommitsToMainBranchCount(15)
                        .withSizeInKb(127));

        authentication.with(gitHubAuthentication, () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> {
                        assertThat(response.getHeaders().getContentType()).isEqualTo(RegistrySerializer.TEXT_PLAIN_004);

                        snapshot.assertThat(response.getBody())
                                .as(canonicalPrometheusRegistry())
                                .matchesSnapshotText();
                    })
                    .verifyComplete();
        });
    }

    @Test
    void test_scrape_with_missing_contributor_stats(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");
        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withThrowingContributorStats());

        authentication.with(gitHubAuthentication, () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> {
                        snapshot.assertThat(response.getBody())
                                .as(canonicalPrometheusRegistry())
                                .matchesSnapshotText();
                    })
                    .verifyComplete();
        });
    }

    @Test
    void test_scrape_with_missing_code_frequency(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");
        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withThrowingCodeFrequency());

        authentication.with(gitHubAuthentication, () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> {
                        snapshot.assertThat(response.getBody())
                                .as(canonicalPrometheusRegistry())
                                .matchesSnapshotText();
                    })
                    .verifyComplete();
        });
    }

    @Test
    void test_successful_scrape_open_metrics(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo",
                Map.of("Accept", "application/openmetrics-text"));

        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withStargazerCount(1337)
                        .withForkCount(5)
                        .withOpenIssueCount(2)
                        .withWatchersCount(1)
                        .withSubscriberCount(4)
                        .withAdditions(50)
                        .withDeletions(-20)
                        .withCommitsToMainBranchCount(15)
                        .withSizeInKb(127));

        authentication.with(gitHubAuthentication, () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> {
                        assertThat(response.getHeaders().getContentType()).isEqualTo(RegistrySerializer.OPEN_METRICS);

                        snapshot.assertThat(response.getBody())
                                .as(canonicalPrometheusRegistry())
                                .matchesSnapshotText();
                    })
                    .verifyComplete();
        });
    }

    @Test
    void test_successful_anonymous_scrape(Snapshot snapshot) throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");
        webProperties.setAllowAnonymousScrape(true);
        final GitHubAuthentication gitHubAuthentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test-repo")
                        .withStargazerCount(1337)
                        .withForkCount(5)
                        .withOpenIssueCount(2)
                        .withWatchersCount(1)
                        .withSubscriberCount(4)
                        .withAdditions(50)
                        .withDeletions(-20)
                        .withCommitsToMainBranchCount(15)
                        .withSizeInKb(127)).setAnonymous(true);

        authentication.with(gitHubAuthentication, () -> {

            StepVerifier.create(serviceCall)
                    .assertNext(response -> snapshot.assertThat(response.getBody())
                            .as(canonicalPrometheusRegistry())
                            .matchesSnapshotText())
                    .verifyComplete();
        });
    }

    @Test
    void client_should_be_blocked_when_abuse_limit_is_exceeded() throws Exception {
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");

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
        final var serviceCall = testClient.getStatsFor("skuzzle", "test-repo");

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
