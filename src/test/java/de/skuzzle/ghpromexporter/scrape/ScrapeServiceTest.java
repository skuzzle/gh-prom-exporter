package de.skuzzle.ghpromexporter.scrape;

import static de.skuzzle.ghpromexporter.github.MockRepositoryBuilder.withName;
import static de.skuzzle.ghpromexporter.github.MockRepositoryGitHubAuthentication.successfulAuthenticationForRepository;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;

public class ScrapeServiceTest {

    private final ScrapeService scrapeService = new ScrapeService();

    @Test
    void testScrape() throws Exception {
        final GitHubAuthentication authentication = successfulAuthenticationForRepository(
                withName("skuzzle", "test")
                        .withStargazerCount(1337)
                        .withForkCount(5)
                        .withOpenIssueCount(2)
                        .withWatchersCount(1)
                        .withSubscriberCount(4)
                        .withAdditions(50)
                        .withDeletions(-20)
                        .withCommitsToMainBranchCount(15)
                        .withSizeInKb(127));

        final ScrapeResult metrics = scrapeService.scrape(authentication,
                new ScrapeTarget("skuzzle", "test"));

        assertThat(metrics.stargazersCount()).isEqualTo(1337);
        assertThat(metrics.forkCount()).isEqualTo(5);
        assertThat(metrics.openIssueCount()).isEqualTo(2);
        assertThat(metrics.watchersCount()).isEqualTo(1);
        assertThat(metrics.subscriberCount()).isEqualTo(4);
        assertThat(metrics.totalAdditions()).isEqualTo(50);
        assertThat(metrics.totalDeletions()).isEqualTo(20);
        assertThat(metrics.commitsToMainBranch()).isEqualTo(15);
        assertThat(metrics.sizeInKb()).isEqualTo(127);
    }
}
