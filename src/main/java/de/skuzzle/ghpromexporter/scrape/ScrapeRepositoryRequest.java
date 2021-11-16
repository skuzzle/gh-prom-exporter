package de.skuzzle.ghpromexporter.scrape;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.web.ApiKey;

public final class ScrapeRepositoryRequest {

    private final String owner;
    private final String repository;
    private final ApiKey apiKey;
    private final GitHubAuthentication githubAuthentication;

    private ScrapeRepositoryRequest(String owner, String repository, ApiKey apiKey,
            GitHubAuthentication gitHubAuthentication) {
        this.owner = owner;
        this.repository = repository;
        this.apiKey = apiKey;
        this.githubAuthentication = gitHubAuthentication;
    }

    public static ScrapeRepositoryRequest of(String owner, String repository, ApiKey apiKey,
            GitHubAuthentication gitHubAuthentication) {
        return new ScrapeRepositoryRequest(owner, repository, apiKey, gitHubAuthentication);
    }

    public GitHubAuthentication githubAuthentication() {
        return this.githubAuthentication;
    }

    public ApiKey apiKey() {
        return this.apiKey;
    }

    public String owner() {
        return this.owner;
    }

    public String name() {
        return this.repository;
    }

    public String repositoryFullName() {
        return owner + "/" + repository;
    }

    @Override
    public String toString() {
        return repositoryFullName();
    }
}
