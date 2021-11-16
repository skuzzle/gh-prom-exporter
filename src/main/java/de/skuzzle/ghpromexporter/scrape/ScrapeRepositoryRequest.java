package de.skuzzle.ghpromexporter.scrape;

public final class ScrapeRepositoryRequest {

    private final String owner;
    private final String repository;

    private ScrapeRepositoryRequest(String owner, String repository) {
        this.owner = owner;
        this.repository = repository;
    }

    public static ScrapeRepositoryRequest of(String owner, String repository) {
        return new ScrapeRepositoryRequest(owner, repository);
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
