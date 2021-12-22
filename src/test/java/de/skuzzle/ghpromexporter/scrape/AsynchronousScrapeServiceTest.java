package de.skuzzle.ghpromexporter.scrape;

import static de.skuzzle.ghpromexporter.github.MockRepositoryGitHubAuthentication.successfulAuthenticationForRepository;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.github.MockRepositoryBuilder;
import de.skuzzle.ghpromexporter.github.MockRepositoryGitHubAuthentication;
import reactor.test.StepVerifier;

@SpringBootTest(properties = {
        "scrape.interval=PT0.9S",
        "scrape.initialDelay=PT0.9S",
})
public class AsynchronousScrapeServiceTest {

    @Autowired
    private AsynchronousScrapeService scrapeService;
    @Autowired
    private RegistrationRepository registrationReporistory;

    @BeforeEach
    @AfterEach
    void cleanup() {
        registrationReporistory.deleteAll();
    }

    private void expectStargazerCount(GitHubAuthentication authentication, String owner, String repository,
            int expectedCount) {
        StepVerifier
                .create(scrapeService.scrapeReactive(authentication, new ScrapeRepositoryRequest(owner, repository)))
                .assertNext(metrics -> assertThat(metrics.stargazersCount()).isEqualTo(expectedCount))
                .verifyComplete();
    }

    @Test
    void metrics_should_be_updated_asynchronously() throws Exception {
        final MockRepositoryBuilder mockedRepo = MockRepositoryBuilder.withName("skuzzle", "test")
                .withStargazerCount(1337);
        final GitHubAuthentication authentication = successfulAuthenticationForRepository(mockedRepo);

        // First scrape: cache miss, scrape synchronously
        expectStargazerCount(authentication, "skuzzle", "test", 1337);

        // Second scrape: updated after initial delay
        mockedRepo.withStargazerCount(1339);
        Thread.sleep(1100);

        expectStargazerCount(authentication, "skuzzle", "test", 1339);

        // Third scrape: update after first fixed delay
        mockedRepo.withStargazerCount(1340);
        Thread.sleep(1100);

        expectStargazerCount(authentication, "skuzzle", "test", 1340);
    }

    @Test
    void registration_should_be_terminated_on_scrape_error() throws Exception {
        final MockRepositoryBuilder mockedRepo = MockRepositoryBuilder.withName("skuzzle", "test")
                .withStargazerCount(1337);
        final MockRepositoryGitHubAuthentication authentication = successfulAuthenticationForRepository(mockedRepo);

        // First scrape: cache miss, scrape synchronously
        expectStargazerCount(authentication, "skuzzle", "test", 1337);

        // Asynchronous update will remove the scraper because of exception
        authentication.modify().withInaccessibleRepository(mockedRepo);
        mockedRepo.withStargazerCount(1340);
        Thread.sleep(1100);

        // Cache miss, update synchronously
        authentication.modify().withRepository(mockedRepo.withStargazerCount(1337));
        expectStargazerCount(authentication, "skuzzle", "test", 1337);
    }
}
