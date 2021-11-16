package de.skuzzle.ghpromexporter.scrape;

public record ScrapeRepositoryRequest(String owner, String repository) {

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
